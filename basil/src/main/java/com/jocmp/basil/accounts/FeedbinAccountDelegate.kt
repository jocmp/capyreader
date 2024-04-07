package com.jocmp.basil.accounts

import android.util.Log
import com.jocmp.basil.AccountDelegate
import com.jocmp.basil.Feed
import com.jocmp.basil.common.nowUTC
import com.jocmp.basil.common.toDateTime
import com.jocmp.basil.common.toDateTimeFromSeconds
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
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
import retrofit2.Response
import java.net.MalformedURLException
import java.net.URL
import java.time.ZonedDateTime

internal class FeedbinAccountDelegate(
    private val database: Database,
    private val feedbin: Feedbin
) : AccountDelegate {
    class FeedNotFound : Exception()
    class SaveFeedFailure : Exception()

    private val articleRecords = ArticleRecords(database)
    private val feedRecords = FeedRecords(database)

    override suspend fun markRead(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.deleteUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
    }

    override suspend fun markUnread(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.createUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
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

        val taggingIDsToDelete = database
            .taggingsQueries
            .findFeedTaggingsToDelete(
                feedID = feed.id,
                excludedNames = folderTitles
            )
            .executeAsList()

        folderTitles.forEach { folderTitle ->
            val request = CreateTaggingRequest(feed_id = feed.id, name = folderTitle)

            withResult(feedbin.createTagging(request)) { tagging ->
                Log.d("FeedbinAccountDelegate", "updateFeed: $tagging")
                database.taggingsQueries.upsert(
                    id = tagging.id,
                    feed_id = tagging.feed_id.toString(),
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

    override suspend fun addStar(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.createStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
    }

    override suspend fun removeStar(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.deleteStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
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

    override suspend fun refresh() {
        val since = maxUpdatedAt()

        refreshFeeds()
        refreshTaggings()
        refreshUnreadEntries()
        refreshStarredEntries()
        refreshAllArticles(since = since)
        fetchMissingArticles()
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
        val icon = icons.find { it.host == host(subscription) }

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

    /** Date in UTC */
    private fun maxUpdatedAt(): String {
        val max = database.articlesQueries.lastUpdatedAt().executeAsOne().MAX

        max ?: return cutoffDate().toString()

        return max.toDateTimeFromSeconds.toString()
    }

    companion object {
        const val MAX_ENTRY_LIMIT = 100
    }
}

private fun cutoffDate(): ZonedDateTime {
    return nowUTC().minusMonths(3)
}

private fun host(subscription: Subscription): String? {
    return try {
        URL(subscription.site_url).host
    } catch (e: MalformedURLException) {
        null
    }
}

private fun <T> withResult(response: Response<T>, handler: (result: T) -> Unit) {
    val result = response.body()

    if (!response.isSuccessful || result == null) {
        return
    }

    handler(result)
}
