package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.readerclient.Category
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.GoogleReader.Companion.BAD_TOKEN_HEADER_KEY
import com.jocmp.readerclient.Item
import com.jocmp.readerclient.ItemRef
import com.jocmp.readerclient.Stream
import com.jocmp.readerclient.Subscription
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import retrofit2.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicReference

internal class ReaderAccountDelegate(
    private val database: Database,
    private val googleReader: GoogleReader,
    httpClient: OkHttpClient = OkHttpClient(),
) : AccountDelegate {
    private var postToken = AtomicReference<String?>(null)
    private val articleContent = ArticleContent(httpClient)
    private val articleRecords = ArticleRecords(database)

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return AddFeedResult.Failure(error = AddFeedResult.AddFeedError.NetworkError())
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun refresh(cutoffDate: ZonedDateTime?): Result<Unit> {
        return try {
            val since = articleRecords.maxUpdatedAt().toEpochSecond()

            refreshFeeds()
            refreshArticles(since = since)

            Result.success(Unit)
        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (e: UnauthorizedError) {
            Result.failure(e)
        }
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> {
        return Result.failure(Throwable(""))
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun fetchFullContent(article: Article): Result<String> {
        article.url ?: return Result.failure(Error("No article url found"))

        return articleContent.fetch(article.url)
    }

    private suspend fun refreshFeeds() {
        withResult(googleReader.subscriptionList()) { result ->
            database.transactionWithErrorHandling {
                result.subscriptions.forEach { subscription ->
                    upsertFeed(subscription)
                    upsertTaggings(subscription)
                }

                cleanUpTaggings(result.subscriptions)
            }
        }
    }

    private fun upsertTaggings(subscription: Subscription) {
        subscription.categories.forEach { category ->
            database.taggingsQueries.upsert(
                id = taggingID(subscription, category),
                feed_id = subscription.id,
                name = category.label.orEmpty(),
            )
        }
    }

    private fun upsertFeed(subscription: Subscription) {
        database.feedsQueries.upsert(
            id = subscription.id,
            subscription_id = subscription.id,
            title = subscription.title,
            feed_url = subscription.url,
            site_url = subscription.htmlUrl,
            favicon_url = subscription.iconUrl.ifBlank { null }
        )
    }

    private fun cleanUpTaggings(subscriptions: List<Subscription>) {
        val excludedIDs = subscriptions.flatMap {
            it.categories.map { category ->
                taggingID(it, category)
            }
        }

        database.taggingsQueries.deleteOrphanedTags(
            excludedIDs = excludedIDs
        )
    }

    private suspend fun refreshArticles(
        since: Long = articleRecords.maxUpdatedAt().toEpochSecond()
    ) {
        refreshStarredItems()
        refreshUnreadItems()
        refreshAllArticles(since = since)
        fetchMissingArticles()
    }

    private suspend fun refreshUnreadItems() {
        withResult(
            googleReader.streamItemsIDs(
                streamID = Stream.READING_LIST.id,
                excludedStreamID = Stream.READ.id
            )
        ) { result ->
            articleRecords.markAllUnread(articleIDs = result.itemRefs.map { it.hexID })
        }
    }

    private suspend fun refreshStarredItems() {
        withResult(googleReader.streamItemsIDs(streamID = Stream.STARRED.id)) { result ->
            articleRecords.markAllStarred(articleIDs = result.itemRefs.map { it.hexID })
        }
    }

    private suspend fun fetchMissingArticles() {
        val ids = articleRecords.findMissingArticles()

        coroutineScope {
            ids.chunked(MAX_PAGINATED_ITEM_LIMIT).map { chunkedIDs ->
                launch {
                    val response = withPostToken {
                        googleReader.streamItemsContents(
                            postToken = postToken.get(),
                            ids = chunkedIDs
                        )
                    }

                    val result = response.body() ?: return@launch

                    saveItems(result.items)
                }
            }
        }
    }

    private suspend fun refreshAllArticles(since: Long) {
        fetchPaginatedItems(
            since = since,
            stream = Stream.READING_LIST
        )
    }

    private suspend fun fetchPaginatedItems(
        since: Long? = null,
        stream: Stream,
        continuation: String? = null,
    ) {
        val response = googleReader.streamItemsIDs(
            streamID = stream.id,
            since = since,
            continuation = continuation,
            excludedStreamID = Stream.READ.id,
            count = MAX_PAGINATED_ITEM_LIMIT,
        )

        val result = response.body() ?: return

        coroutineScope {
            launch {
                fetchItemContents(result.itemRefs)
            }
        }

        val nextContinuation = result.continuation ?: return

        fetchPaginatedItems(
            since = since,
            stream = stream,
            continuation = nextContinuation
        )
    }

    private suspend fun fetchItemContents(items: List<ItemRef>) {
        val response = withPostToken {
            googleReader.streamItemsContents(
                postToken = postToken.get(),
                ids = items.map { it.hexID }
            )
        }

        val result = response.body() ?: return

        saveItems(result.items)
    }

    private fun saveItems(items: List<Item>) {
        database.transactionWithErrorHandling {
            items.forEach { item ->
                val updated = TimeHelpers.nowUTC().toEpochSecond()

                database.articlesQueries.create(
                    id = item.hexID,
                    feed_id = item.origin.streamId,
                    title = item.title,
                    author = item.author,
                    content_html = item.content?.content ?: item.summary.content,
                    extracted_content_url = null,
                    summary = Jsoup.parse(item.summary.content).text(),
                    url = item.canonical.firstOrNull()?.href,
                    image_url = item.image?.href,
                    published_at = item.published
                )

                database.articlesQueries.updateStatus(
                    article_id = item.hexID,
                    updated_at = updated,
                    read = true
                )
            }
        }
    }

    private suspend fun <T> withPostToken(handler: suspend () -> Response<T>): Response<T> {
        val response = handler()

        val isBadToken = response
            .headers()
            .get(BAD_TOKEN_HEADER_KEY)
            .orEmpty()
            .toBoolean()

        if (!isBadToken) {
            return response
        }

        try {
            postToken.set(googleReader.token().body())

            return handler()
        } catch (exception: IOException) {
            return response
        }
    }

    private fun taggingID(subscription: Subscription, category: Category): String {
        return "${subscription.id}:${category.id}"
    }


    companion object {
        const val MAX_PAGINATED_ITEM_LIMIT = 100
    }
}
