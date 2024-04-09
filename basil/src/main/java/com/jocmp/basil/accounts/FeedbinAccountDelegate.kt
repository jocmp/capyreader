package com.jocmp.basil.accounts

import com.jocmp.basil.AccountDelegate
import com.jocmp.basil.Feed
import com.jocmp.basil.common.host
import com.jocmp.basil.common.nowUTC
import com.jocmp.basil.common.toDateTime
import com.jocmp.basil.common.withResult
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.basil.persistence.TaggingRecords
import com.jocmp.feedbinclient.CreateSubscriptionRequest
import com.jocmp.feedbinclient.CreateTaggingRequest
import com.jocmp.feedbinclient.Entry
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedbinclient.Icon
import com.jocmp.feedbinclient.StarredEntriesRequest
import com.jocmp.feedbinclient.Subscription
import com.jocmp.feedbinclient.UnreadEntriesRequest
import com.jocmp.feedbinclient.UpdateSubscriptionRequest
import com.jocmp.feedbinclient.pagingInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.UnknownHostException
import java.time.ZonedDateTime

internal class FeedbinAccountDelegate(
    private val database: Database,
    private val feedbin: Feedbin
) : AccountDelegate {
    class FeedNotFound : Exception()
    class SaveFeedFailure : Exception()

    private val articleRecords = ArticleRecords(database)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.deleteUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
        }
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.createUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> {
        if (title != feed.title) {
            feedRecords.updateTitle(feed = feed, title = title)

            feedbin.updateSubscription(
                subscriptionID = feed.subscriptionID,
                body = UpdateSubscriptionRequest(title = title)
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
                    id = tagging.id,
                    feedID = tagging.feed_id.toString(),
                    name = tagging.name
                )
            }
        }

        taggingIDsToDelete.forEach { taggingID ->
            val result = feedbin.deleteTagging(taggingID = taggingID.toString())

            if (result.isSuccessful) {
                database.taggingsQueries.deleteTagging(taggingID)
            }
        }

        val updatedFeed = feedRecords.findBy(feed.id)

        return if (updatedFeed != null) {
            Result.success(updatedFeed)
        } else {
            Result.failure(Throwable("Feed not found"))
        }
    }

    override suspend fun removeFeed(feedID: String) {
        val feed = feedRecords.findBy(feedID) ?: return

        val result = feedbin.deleteSubscription(subscriptionID = feed.subscriptionID)

        if (result.isSuccessful) {
            feedRecords.removeFeed(feedID = feedID)
        }
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.createStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
        }
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            feedbin.deleteStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
        }
    }

    override suspend fun addFeed(url: String): Result<AddFeedResult> {
        val response = feedbin.createSubscription(CreateSubscriptionRequest(feed_url = url))
        val subscription = response.body()
        val errorBody = response.errorBody()?.string()

        if (response.code() > 300) {
            return Result.failure(FeedNotFound())
        }

        return if (subscription != null) {
            val icons = fetchIcons()
            upsertFeed(subscription, icons)

            val feed = feedRecords.findBy(subscription.feed_id.toString())

            if (feed != null) {
                Result.success(AddFeedResult.Success(feed))
            } else {
                Result.failure(SaveFeedFailure())
            }
        } else {
            val decodedChoices = Json.decodeFromString<List<SubscriptionChoice>>(errorBody!!)

            val choices = decodedChoices.map {
                FeedOption(feedURL = it.feed_url, title = it.title)
            }

            Result.success(AddFeedResult.MultipleChoices(choices))
        }
    }

    override suspend fun refresh(): Result<Unit> {
        val since = articleRecords.maxUpdatedAt()

        return try {
            refreshFeeds()
            refreshTaggings()
            refreshUnreadEntries()
            refreshStarredEntries()
            refreshAllArticles(since = since)
            fetchMissingArticles()

            Result.success(Unit)
        } catch (exception: UnknownHostException) {
            Result.failure(exception)
        }
    }

    private suspend fun refreshFeeds() {
        val icons = fetchIcons()

        withResult(feedbin.subscriptions()) { subscriptions ->
            subscriptions.forEach { subscription ->
                upsertFeed(subscription, icons)
            }

            val feedsToRemove = subscriptions.map { it.feed_id.toString() }

            database.feedsQueries.deleteAllExcept(feedsToRemove)
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
            favicon_url = icon?.url
        )
    }

    private suspend fun refreshTaggings() {
        withResult(feedbin.taggings()) { taggings ->
            database.transaction {
                taggings.forEach { tagging ->
                    database.taggingsQueries.upsert(
                        id = tagging.id,
                        feed_id = tagging.feed_id.toString(),
                        name = tagging.name,
                    )
                }

                database.taggingsQueries.deleteOrphanedTags(excludedIDs = taggings.map { it.id })
            }
        }
    }

    private suspend fun refreshAllArticles(since: String) {
        fetchPaginatedEntries(since = since)
    }

    private suspend fun fetchMissingArticles() {
        val ids = articleRecords.findMissingArticles()

        ids.chunked(MAX_ENTRY_LIMIT).map { chunkedIDs ->
            coroutineScope {
                launch {
                    fetchPaginatedEntries(ids = chunkedIDs)
                }
            }
        }
    }

    private suspend fun fetchPaginatedEntries(
        since: String? = null,
        nextPage: Int? = 1,
        ids: List<Long>? = null
    ) {
        nextPage ?: return

        val response = feedbin.entries(
            since = since,
            page = nextPage.toString(),
            ids = ids?.joinToString(",")
        )
        val entries = response.body()

        if (entries != null) {
            saveEntries(entries)
        }

        fetchPaginatedEntries(
            since = since,
            nextPage = response.pagingInfo?.nextPage,
            ids = ids
        )
    }

    private fun saveEntries(entries: List<Entry>, updatedAt: ZonedDateTime = nowUTC()) {
        database.transaction {
            entries.forEach { entry ->
                val updated = updatedAt.toEpochSecond()

                database.articlesQueries.create(
                    id = entry.id.toString(),
                    feed_id = entry.feed_id.toString(),
                    title = entry.title,
                    content_html = entry.content,
                    url = entry.url,
                    summary = entry.summary,
                    image_url = entry.images?.size_1?.cdn_url,
                    published_at = entry.published.toDateTime?.toEpochSecond(),
                )

                database.articlesQueries.updateStatus(
                    article_id = entry.id.toString(),
                    updated_at = updated
                )
            }
        }
    }

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
    }
}

private suspend fun withErrorHandling(func: suspend () -> Unit): Result<Unit> {
    return try {
        func()
        Result.success(Unit)
    } catch (e: UnknownHostException) {
        return Result.failure(e)
    }
}
