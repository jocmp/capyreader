package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.ValidationError
import com.jocmp.capy.accounts.feedbin.FeedbinAccountDelegate.Companion.MAX_CREATE_UNREAD_LIMIT
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.SavedSearchRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedfinder.withProtocol
import com.jocmp.readerclient.Category
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.GoogleReader.Companion.BAD_TOKEN_HEADER_KEY
import com.jocmp.readerclient.Item
import com.jocmp.readerclient.ItemRef
import com.jocmp.readerclient.Stream
import com.jocmp.readerclient.Stream.Read
import com.jocmp.readerclient.Stream.UserLabel
import com.jocmp.readerclient.Subscription
import com.jocmp.readerclient.SubscriptionEditAction
import com.jocmp.readerclient.SubscriptionQuickAddResult
import com.jocmp.readerclient.Tag
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
    private val source: Source,
    private val database: Database,
    private val googleReader: GoogleReader,
) : AccountDelegate {
    private var postToken = AtomicReference<String?>(null)
    private val articleRecords = ArticleRecords(database)
    private val feedRecords = FeedRecords(database)
    private val enclosureRecords = EnclosureRecords(database)
    private val taggingRecords = TaggingRecords(database)
    private val savedSearchRecords = SavedSearchRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        return withErrorHandling {
            if (filter.hasArticlesSelected()) {
                refreshTopLevelArticles()
            } else {
                refreshArticles(filter.toStream(source))
            }
        }
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        val results = articleIDs.chunked(MAX_CREATE_UNREAD_LIMIT).map { batchIDs ->
            editTag(ids = batchIDs, addTag = Stream.Read())
        }

        return results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, removeTag = Stream.Read())
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, addTag = Stream.Starred())
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return editTag(ids = articleIDs, removeTag = Stream.Starred())
    }

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        savedSearchRecords.upsertArticle(articleID = articleID, savedSearchID = savedSearchID)

        return editTag(ids = listOf(articleID), addTag = Stream.UserLabel(savedSearchID)).onFailure {
            savedSearchRecords.removeArticle(articleID = articleID, savedSearchID = savedSearchID)
        }
    }

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        savedSearchRecords.removeArticle(articleID = articleID, savedSearchID = savedSearchID)

        return editTag(ids = listOf(articleID), removeTag = Stream.UserLabel(savedSearchID)).onFailure {
            savedSearchRecords.upsertArticle(articleID = articleID, savedSearchID = savedSearchID)
        }
    }

    override suspend fun createSavedSearch(name: String): Result<String> {
        val labelID = userLabel(name)
        savedSearchRecords.upsert(id = labelID, name = name)
        return Result.success(labelID)
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

            val feed = feedRecords.find(subscription.id)

            if (feed != null) {
                coroutineScope {
                    launchIO { refreshArticles(Stream.Feed(feed.id)) }
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

            feedRecords.find(feed.id)
        }
    }

    override suspend fun updateFolder(oldTitle: String, newTitle: String): Result<Unit> {
        return withErrorHandling {
            val response = withPostToken {
                googleReader.renameTag(
                    streamID = Stream.Label(oldTitle).id,
                    destination = Stream.Label(newTitle).id,
                    postToken = postToken.get()
                )
            }

            if (!response.isSuccessful) {
                throw ValidationError(response.message())
            }
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

    override suspend fun removeFolder(folderTitle: String): Result<Unit> {
        return withErrorHandling {
            val response = withPostToken {
                googleReader.disableTag(
                    streamID = Stream.Label(folderTitle).id,
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

    private suspend fun refreshTopLevelArticles() {
        refreshFeeds()
        refreshAllSavedSearches()
        refreshArticleState()
        fetchMissingArticles()
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
            favicon_url = subscription.iconUrl?.ifBlank {
                CapyLog.warn(tag("blank_icon"), mapOf("feed_url" to subscription.url))
                null
            },
            priority = subscription.frssPriority
        )

        upsertTaggings(subscription)
    }

    private fun cleanUpTaggings(subscriptions: List<Subscription>) {
        val excludedIDs = subscriptions.flatMap {
            it.categories.map { category ->
                taggingID(it, category)
            }
        }

        taggingRecords.deleteOrphaned(excludedIDs = excludedIDs)
    }

    private suspend fun refreshAllSavedSearches() {
        withResult(googleReader.tagList()) { result ->
            database.transactionWithErrorHandling {
                val tags = result.tags.filter { it.type == Tag.Type.TAG }

                tags.forEach {
                    upsertSavedSearch(it)
                }

                savedSearchRecords.deleteOrphaned(excludedIDs = tags.map { it.id })
            }
        }
    }

    private fun upsertSavedSearch(tag: Tag) {
        savedSearchRecords.upsert(
            id = tag.id,
            name = tag.name
        )
    }

    private suspend fun refreshArticleState() {
        refreshStarredItems()
        refreshUnreadItems()
    }

    private suspend fun refreshUnreadItems() {
        withResult(
            googleReader.streamItemsIDs(
                stream = Stream.ReadingList(),
                excludedStream = Read()
            )
        ) { result ->
            articleRecords.markAllUnread(articleIDs = result.itemRefs.map { it.hexID })
        }
    }

    private suspend fun refreshStarredItems() {
        withResult(googleReader.streamItemsIDs(stream = Stream.Starred())) { result ->
            articleRecords.markAllStarred(articleIDs = result.itemRefs.map { it.hexID })
        }
    }

    /**
     * This is a slightly different algorithm than [refreshTopLevelArticles].
     *
     *   - Assume the category (folder or feed) exists so it skips refreshing the subscription list
     *   - Fetches up to 10k IDs
     *   - On result, the [fetchMissingArticles] will only fetch articles that are not already
     *     saved
     */
    private suspend fun refreshArticles(stream: Stream) {
        if (stream !is Stream.Feed) {
            refreshFeeds()
        }

        if (stream is Stream.UserLabel) {
            fetchPaginatedArticles(stream = stream)
        } else {
            refreshArticleState()

            withResult(googleReader.streamItemsIDs(stream = stream)) { result ->
                articleRecords.createStatuses(articleIDs = result.itemRefs.map { it.hexID })
            }

            fetchMissingArticles()
        }
    }

    private suspend fun fetchMissingArticles() {
        val ids = articleRecords.findMissingArticles()

        if (ids.isEmpty()) {
            return
        }

        coroutineScope {
            ids.chunked(MAX_PAGINATED_ITEM_LIMIT).map { chunkedIDs ->
                launch {
                    val response = withPostToken {
                        googleReader.streamItemsContents(
                            postToken = postToken.get(),
                            ids = chunkedIDs.map { it }
                        )
                    }

                    val result = response.body() ?: return@launch

                    saveArticles(result.items)
                }
            }
        }
    }

    private suspend fun fetchPaginatedArticles(
        since: Long? = null,
        stream: Stream,
        continuation: String? = null,
    ) {
        val response = googleReader.streamItemsIDs(
            stream = stream,
            since = since,
            continuation = continuation,
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

        fetchPaginatedArticles(
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

        saveArticles(result.items)
    }

    private fun saveArticles(items: List<Item>) {
        database.transactionWithErrorHandling {
            val labels = savedSearchRecords.allIDs()

            items.forEach { item ->
                val updated = TimeHelpers.nowUTC()
                val enclosures = ReaderEnclosureParsing.validEnclosures(item)
                val enclosureType = enclosures.firstOrNull()?.type

                database.articlesQueries.create(
                    id = item.hexID,
                    feed_id = item.origin.streamId,
                    title = item.title,
                    author = item.author,
                    content_html = item.content?.content ?: item.summary.content,
                    extracted_content_url = null,
                    summary = item.summary.content?.let { Jsoup.parse(it).text() },
                    url = item.canonical.firstOrNull()?.href,
                    image_url = ReaderEnclosureParsing.parsedImageURL(item),
                    published_at = item.published,
                    enclosure_type = enclosureType,
                )

                articleRecords.updateStatus(
                    articleID = item.hexID,
                    updatedAt = updated,
                    read = item.read,
                    starred = item.starred
                )

                item.categories?.forEach { category ->
                    if (labels.contains(category)) {
                        savedSearchRecords.upsertArticle(
                            articleID = item.hexID,
                            savedSearchID = category,
                        )
                    }
                }

                item.categories?.let { categories ->
                    savedSearchRecords.removeArticleBySavedSearchIDs(
                        articleID = item.hexID,
                        excludedIDs = categories
                    )
                }

                enclosures.forEach {
                    enclosureRecords.create(
                        url = it.url.toString(),
                        type = it.type,
                        articleID = item.hexID,
                    )
                }
            }
        }
    }

    private suspend fun editTag(
        ids: List<String>,
        addTag: Stream? = null,
        removeTag: Stream? = null,
    ): Result<Unit> {
        return withErrorHandling {
            val response = withPostToken {
                googleReader.editTag(
                    ids,
                    postToken = postToken.get(),
                    addTag = addTag?.id,
                    removeTag = removeTag?.id
                )
            }

            if (!response.isSuccessful) {
                throw ValidationError(response.message())
            }
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
            iconUrl = "",
            frssPriority = null
        )
    }

private fun ArticleFilter.toStream(source: Source): Stream {
    return when (this) {
        is ArticleFilter.Articles, is ArticleFilter.Today -> Read()
        is ArticleFilter.Feeds -> Stream.Feed(feedID)
        is ArticleFilter.Folders -> folderStream(this, source)
        is ArticleFilter.SavedSearches -> UserLabel(savedSearchID)
    }
}

/**
 * Default to reading list for folders since Miniflux doesn't support label lookups
 */
private fun folderStream(filter: ArticleFilter.Folders, source: Source): Stream {
    return if (source == Source.FRESHRSS) {
        Stream.Label(filter.folderTitle)
    } else {
        Stream.ReadingList()
    }
}

private fun userLabel(title: String): String {
    return "user/-/label/${title}"
}

private const val DEFAULT_FEED_NAME = "Untitled"
