package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.ValidationError
import com.jocmp.capy.accounts.feedbin.FeedbinAccountDelegate.Companion.MAX_CREATE_UNREAD_LIMIT
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedfinder.withProtocol
import com.jocmp.readerclient.Category
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.GoogleReader.Companion.BAD_TOKEN_HEADER_KEY
import com.jocmp.readerclient.Item
import com.jocmp.readerclient.ItemRef
import com.jocmp.readerclient.Stream
import com.jocmp.readerclient.Subscription
import com.jocmp.readerclient.SubscriptionEditAction
import com.jocmp.readerclient.SubscriptionQuickAddResult
import com.jocmp.readerclient.ext.editSubscription
import com.jocmp.readerclient.ext.streamItemsIDs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicReference

internal class ReaderAccountDelegate(
    private val database: Database,
    private val googleReader: GoogleReader,
) : AccountDelegate {
    private var postToken = AtomicReference<String?>(null)
    private val articleRecords = ArticleRecords(database)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)

    override suspend fun refresh(cutoffDate: ZonedDateTime?): Result<Unit> {
        return withErrorHandling {
            val since = articleRecords.maxUpdatedAt().toEpochSecond()

            refreshFeeds()
            refreshArticles(since = since)
        }
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        val results = articleIDs.chunked(MAX_CREATE_UNREAD_LIMIT).map { batchIDs ->
            editTag(ids = batchIDs, addTag = Stream.READ)
        }

        return results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, removeTag = Stream.READ)
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, addTag = Stream.STARRED)
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, removeTag = Stream.STARRED)
    }

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return try {
            val result = withPostToken {
                googleReader
                    .quickAddSubscription(url = url.withProtocol, postToken = postToken.get())
            }.body()

            val subscription = result?.toSubscription ?: return AddFeedResult.feedNotFound()

            try {
                refreshFeeds()
            } catch (e: Throwable) {
                upsertFeed(subscription)
            }

            val feed = feedRecords.findBy(subscription.id)

            if (feed != null) {
                coroutineScope {
                    launch { refreshArticles() }
                }

                AddFeedResult.Success(feed)
            } else {
                AddFeedResult.saveFailure()
            }
        } catch (e: IOException) {
            AddFeedResult.networkError()
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> {
        if (folderTitles.size != 1) {
            return Result.failure(InvalidFoldersError())
        }

        return withErrorHandling {
            val addCategoryID = folderTitles.map {
                userLabel(it)
            }

            val response = withPostToken {
                googleReader.editSubscription(
                    id = feed.id,
                    title = title,
                    action = SubscriptionEditAction.EDIT,
                    addCategoryID = addCategoryID.first(),
                    postToken = postToken.get(),
                )
            }

            if (!response.isSuccessful) {
                throw ValidationError(response.message())
            }

            database.transactionWithErrorHandling {
                feedRecords.update(
                    feedID = feed.id,
                    title = title,
                )

                folderTitles.forEach { title ->
                    taggingRecords.upsert(
                        id = taggingID(feed, title),
                        feedID = feed.id,
                        name = title
                    )
                }

                val taggingIDsToDelete = taggingRecords.findFeedTaggingsToDelete(
                    feed = feed,
                    excludedTaggingNames = folderTitles
                )

                taggingIDsToDelete.forEach { taggingID ->
                    taggingRecords.deleteTagging(taggingID = taggingID)
                }
            }

            feedRecords.findBy(feed.id)
        }
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        return withErrorHandling {
            val response = withPostToken {
                googleReader.editSubscription(
                    id = feed.id,
                    action = SubscriptionEditAction.UNSUBSCRIBE,
                    postToken = postToken.get()
                )

            }

            if (!response.isSuccessful) {
                throw ValidationError(response.message())
            }
        }
    }

    private suspend fun refreshFeeds() {
        withResult(googleReader.subscriptionList()) { result ->
            val subscriptions = result.subscriptions

            database.transactionWithErrorHandling {
                subscriptions.forEach { subscription ->
                    upsertFeed(subscription)
                }

                database.feedsQueries.deleteAllExcept(subscriptions.map { it.id })

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
            favicon_url = subscription.iconUrl.ifBlank {
                CapyLog.warn(tag("blank_icon"), mapOf("feed_url" to subscription.url))
                null
            }
        )

        upsertTaggings(subscription)
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
                stream = Stream.READING_LIST,
                excludedStream = Stream.READ
            )
        ) { result ->
            articleRecords.markAllUnread(articleIDs = result.itemRefs.map { it.hexID })
        }
    }

    private suspend fun refreshStarredItems() {
        withResult(googleReader.streamItemsIDs(stream = Stream.STARRED)) { result ->
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
            stream = stream,
            since = since,
            continuation = continuation,
            count = MAX_PAGINATED_ITEM_LIMIT,
        )

        val result = response.body()

        if (result == null || result.itemRefs.isEmpty()) {
            return
        }

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

    private suspend fun editTag(
        ids: List<String>,
        addTag: Stream? = null,
        removeTag: Stream? = null,
    ): Result<Unit> {
        return withErrorHandling {
            withPostToken {
                googleReader.editTag(
                    ids,
                    postToken = postToken.get(),
                    addTag = addTag?.id,
                    removeTag = removeTag?.id
                )
            }

            Unit
        }
    }

    private suspend fun <T> withPostToken(handler: suspend () -> Response<T>): Response<T> {
        if (postToken.get() == null) {
            fetchToken()
        }

        val response = handler()

        val isBadToken = response
            .headers()
            .get(BAD_TOKEN_HEADER_KEY)
            .orEmpty()
            .toBoolean()

        if (!isBadToken) {
            return response
        }

        fetchToken()

        return handler()
    }

    private suspend fun fetchToken() {
        try {
            postToken.set(googleReader.token().body())
        } catch (exception: IOException) {
            CapyLog.error(tag("post_token"), exception)
            // continue
        }
    }

    private fun taggingID(feed: Feed, categoryTitle: String): String {
        return "${feed.id}:${userLabel(categoryTitle)}"
    }

    private fun taggingID(subscription: Subscription, category: Category): String {
        return "${subscription.id}:${category.id}"
    }

    companion object {
        const val MAX_PAGINATED_ITEM_LIMIT = 100

        private const val TAG = "reader"

        private fun tag(path: String) = "$TAG.$path"
    }
}

private val SubscriptionQuickAddResult.toSubscription: Subscription?
    get() {
        val id = streamId ?: return null
        val url = query ?: return null

        return Subscription(
            id = id,
            title = streamName ?: DEFAULT_FEED_NAME,
            categories = emptyList(),
            url = url,
            htmlUrl = "",
            iconUrl = ""
        )
    }

private fun userLabel(title: String): String {
    return "user/-/label/${title}"
}

private const val DEFAULT_FEED_NAME = "Untitled"
