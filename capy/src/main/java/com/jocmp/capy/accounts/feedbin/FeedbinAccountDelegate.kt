package com.jocmp.capy.accounts.feedbin

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import com.jocmp.capy.accounts.SubscriptionChoice
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.host
import com.jocmp.capy.common.toDateTime
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.SavedSearchRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedbinclient.CreateSubscriptionRequest
import com.jocmp.feedbinclient.CreateTaggingRequest
import com.jocmp.feedbinclient.DeleteTagRequest
import com.jocmp.feedbinclient.Entry
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedbinclient.Icon
import com.jocmp.feedbinclient.SavedSearch
import com.jocmp.feedbinclient.StarredEntriesRequest
import com.jocmp.feedbinclient.Subscription
import com.jocmp.feedbinclient.UnreadEntriesRequest
import com.jocmp.feedbinclient.UpdateSubscriptionRequest
import com.jocmp.feedbinclient.UpdateTagRequest
import com.jocmp.feedbinclient.pagingInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okio.IOException
import org.jsoup.Jsoup
import java.time.ZonedDateTime

internal class FeedbinAccountDelegate(
    private val database: Database,
    private val feedbin: Feedbin
) : AccountDelegate {
    private val articleRecords = ArticleRecords(database)
    private val enclosureRecords = EnclosureRecords(database)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)
    private val savedSearchRecords = SavedSearchRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        return try {
            refreshFeeds()
            refreshTaggings()
            refreshSavedSearches()
            refreshArticles(since = maxArrivedAt())

            Result.success(Unit)
        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (e: UnauthorizedError) {
            Result.failure(e)
        }
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            entryIDs.chunked(MAX_CREATE_UNREAD_LIMIT).map { batchIDs ->
                feedbin.deleteUnreadEntries(UnreadEntriesRequest(unread_entries = batchIDs))
            }
            Unit
        }
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.createUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
            Unit
        }
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.createStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
            Unit
        }
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.deleteStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
            Unit
        }
    }

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun createSavedSearch(name: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return try {
            val response = feedbin.createSubscription(CreateSubscriptionRequest(feed_url = url))
            val subscription = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.code() > 300) {
                return AddFeedResult.Failure(AddFeedResult.Error.FeedNotFound())
            }

            return if (subscription != null) {
                val icons = fetchIcons()
                upsertFeed(subscription, icons)

                val feed = feedRecords.find(subscription.feed_id.toString())

                if (feed != null) {
                    coroutineScope {
                        launch { refreshArticles() }
                    }

                    AddFeedResult.Success(feed)
                } else {
                    AddFeedResult.Failure(AddFeedResult.Error.SaveFailure())
                }
            } else {
                val decodedChoices = Json.decodeFromString<List<SubscriptionChoice>>(errorBody!!)

                val choices = decodedChoices.map {
                    FeedOption(feedURL = it.feed_url, title = it.title)
                }

                AddFeedResult.MultipleChoices(choices)
            }
        } catch (e: IOException) {
            AddFeedResult.networkError()
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed> = withErrorHandling {
        if (title != feed.title) {
            feedbin.updateSubscription(
                subscriptionID = feed.subscriptionID,
                body = UpdateSubscriptionRequest(title = title)
            )

            feedRecords.update(
                feedID = feed.id,
                title = title,
            )
        }

        val taggingIDsToDelete = taggingRecords.findFeedTaggingsToDelete(
            feed = feed,
            excludedTaggingNames = folderTitles
        )

        folderTitles.forEach { folderTitle ->
            val request = CreateTaggingRequest(feed_id = feed.id, name = folderTitle)

            withResult(feedbin.createTagging(request)) { tagging ->
                taggingRecords.upsert(
                    id = tagging.id.toString(),
                    feedID = tagging.feed_id.toString(),
                    name = tagging.name
                )
            }
        }

        taggingIDsToDelete.forEach { taggingID ->
            val result = feedbin.deleteTagging(taggingID = taggingID)

            if (result.isSuccessful) {
                taggingRecords.deleteTagging(taggingID = taggingID)
            }
        }

        feedRecords.find(feed.id)
    }

    override suspend fun updateFolder(
        oldTitle: String,
        newTitle: String
    ): Result<Unit> = withErrorHandling {
        feedbin.updateTag(UpdateTagRequest(old_name = oldTitle, new_name = newTitle))

        Unit
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> = withErrorHandling {
        feedbin.deleteSubscription(subscriptionID = feed.subscriptionID)

        Unit
    }

    override suspend fun removeFolder(folderTitle: String): Result<Unit> = withErrorHandling {
        feedbin.deleteTag(DeleteTagRequest(name = folderTitle))

        Unit
    }

    private suspend fun refreshArticles(since: String = maxArrivedAt()) {
        refreshStarredEntries()
        refreshUnreadEntries()
        refreshAllArticles(since = since)
        fetchMissingArticles()
    }

    private suspend fun refreshFeeds() {
        val icons = fetchIcons()

        withResult(feedbin.subscriptions()) { subscriptions ->
            database.transactionWithErrorHandling {
                subscriptions.forEach { subscription ->
                    upsertFeed(subscription, icons)
                }
            }

            val feedsToKeep = subscriptions.map { it.feed_id.toString() }

            database.feedsQueries.deleteAllExcept(feedsToKeep)
        }
    }

    private suspend fun refreshUnreadEntries() {
        withResult(feedbin.unreadEntries()) { ids ->
            articleRecords.markAllUnread(articleIDs = ids.map { it.toString() })
        }
    }

    private suspend fun refreshStarredEntries() {
        withResult(feedbin.starredEntries()) { ids ->
            articleRecords.markAllStarred(articleIDs = ids.map { it.toString() })
        }
    }

    private fun upsertFeed(subscription: Subscription, icons: List<Icon>) {
        val icon = icons.find { it.host == subscription.host }

        database.feedsQueries.upsert(
            id = subscription.feed_id.toString(),
            subscription_id = subscription.id.toString(),
            title = subscription.title,
            feed_url = subscription.feed_url,
            site_url = subscription.site_url,
            favicon_url = icon?.url,
            priority = null
        )
    }

    private suspend fun refreshTaggings() {
        withResult(feedbin.taggings()) { taggings ->
            database.transactionWithErrorHandling {
                taggings.forEach { tagging ->
                    database.taggingsQueries.upsert(
                        id = tagging.id.toString(),
                        feed_id = tagging.feed_id.toString(),
                        name = tagging.name,
                    )
                }

                database.taggingsQueries.deleteOrphanedTags(
                    excludedIDs = taggings.map { it.id.toString() }
                )
            }
        }
    }

    private suspend fun refreshSavedSearches() {
        withResult(feedbin.savedSearches()) { savedSearches ->
            database.transactionWithErrorHandling {
                savedSearches.forEach {
                    upsertSavedSearch(it)
                }

                savedSearchRecords.deleteOrphaned(
                    excludedIDs = savedSearches.map { it.id.toString() }
                )
            }
        }

        coroutineScope {
            savedSearchRecords.allIDs()
                .forEach { savedSearchID ->
                    launch {
                        fetchSavedSearchArticles(savedSearchID = savedSearchID)
                    }
                }
        }
    }

    private suspend fun fetchSavedSearchArticles(savedSearchID: String) {
        val ids = feedbin.savedSearchEntries(savedSearchID = savedSearchID).body() ?: return

        savedSearchRecords.deleteOrphanedEntries(
            savedSearchID,
            excludedIDs = ids.map { it.toString() }
        )

        ids.chunked(MAX_ENTRY_LIMIT).map { chunkedIDs ->
            fetchPaginatedEntries(
                ids = chunkedIDs,
                savedSearchID = savedSearchID
            )
        }
    }

    private suspend fun refreshAllArticles(since: String) {
        fetchPaginatedEntries(since = since)
    }

    private suspend fun fetchMissingArticles() {
        val ids = articleRecords.findMissingArticles()

        coroutineScope {
            ids.chunked(MAX_ENTRY_LIMIT).map { chunkedIDs ->
                launch {
                    fetchPaginatedEntries(ids = chunkedIDs.map { it.toLong() })
                }
            }
        }
    }

    private suspend fun fetchPaginatedEntries(
        since: String? = null,
        nextPage: Int? = 1,
        ids: List<Long>? = null,
        savedSearchID: String? = null,
    ) {
        nextPage ?: return

        val response = feedbin.entries(
            since = since,
            page = nextPage.toString(),
            ids = ids?.joinToString(",")
        )
        val entries = response.body()

        if (entries != null) {
            saveEntries(entries, savedSearchID = savedSearchID)
        }

        fetchPaginatedEntries(
            since = since,
            nextPage = response.pagingInfo?.nextPage,
            ids = ids
        )
    }

    private fun saveEntries(
        entries: List<Entry>,
        savedSearchID: String? = null,
    ) {
        database.transactionWithErrorHandling {
            entries.forEach { entry ->
                val updated = TimeHelpers.nowUTC()
                val articleID = entry.id.toString()
                val enclosure = entry.enclosure
                val enclosureType = enclosure?.enclosure_type

                database.articlesQueries.create(
                    id = articleID,
                    feed_id = entry.feed_id.toString(),
                    title = entry.title?.let { Jsoup.parse(it).text() },
                    author = entry.author,
                    content_html = entry.content,
                    extracted_content_url = entry.extracted_content_url,
                    url = entry.url,
                    summary = entry.summary,
                    image_url = entry.images?.size_1?.cdn_url,
                    published_at = entry.published.toDateTime?.toEpochSecond(),
                    enclosure_type = enclosureType,
                )

                articleRecords.createStatus(
                    articleID = articleID,
                    updatedAt = updated,
                    read = true
                )

                if (enclosure != null && enclosureType != null) {
                    enclosureRecords.create(
                        url = enclosure.enclosure_url,
                        type = enclosureType,
                        articleID = articleID,
                        itunesDurationSeconds = enclosure.itunes_duration,
                        itunesImage = enclosure.itunes_image,
                    )
                }

                if (savedSearchID != null) {
                    savedSearchRecords.upsertArticle(
                        articleID = articleID,
                        savedSearchID = savedSearchID,
                    )
                }
            }
        }
    }

    private fun upsertSavedSearch(savedSearch: SavedSearch) {
        savedSearchRecords.upsert(
            id = savedSearch.id.toString(),
            name = savedSearch.name,
            query = savedSearch.query
        )
    }

    private fun maxArrivedAt() = articleRecords.maxArrivedAt().toString()

    private suspend fun fetchIcons(): List<Icon> {
        val response = feedbin.icons()
        val result = response.body()

        if (!response.isSuccessful || result == null) {
            return listOf()
        }

        return result
    }

    companion object {
        const val MAX_ENTRY_LIMIT = 100
        const val MAX_CREATE_UNREAD_LIMIT = 1_000
    }
}
