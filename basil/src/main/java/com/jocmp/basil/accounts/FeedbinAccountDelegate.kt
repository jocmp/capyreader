package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import com.jocmp.basil.common.nowUTCInSeconds
import com.jocmp.basil.common.toDateTime
import com.jocmp.basil.common.toDateTimeFromSeconds
import com.jocmp.basil.db.Database
import com.jocmp.feedbinclient.CreateSubscriptionRequest
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedbinclient.StarredEntriesRequest
import com.jocmp.feedbinclient.Subscription
import com.jocmp.feedbinclient.UnreadEntriesRequest
import retrofit2.Response

internal class FeedbinAccountDelegate(
    private val database: Database,
    private val feedbin: Feedbin
) {
    fun fetchAll(feed: Feed): List<ParsedItem> {
        return emptyList()
    }

    suspend fun markRead(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.deleteUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
    }

    suspend fun markUnread(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.createUnreadEntries(UnreadEntriesRequest(unread_entries = entryIDs))
    }

    suspend fun addStar(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.createStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
    }

    suspend fun removeStar(articleIDs: List<String>) {
        val entryIDs = articleIDs.map { it.toLong() }

        feedbin.deleteStarredEntries(StarredEntriesRequest(starred_entries = entryIDs))
    }

    suspend fun addFeed(url: String): Result<AddFeedResult> {
        val response = feedbin.createSubscription(CreateSubscriptionRequest(feed_url = url))
        val subscription = response.body()
        val errorBody = response.errorBody()

        if (response.code() > 300) {
            return Result.failure(Throwable(response.code().toString()))
        }

        val result = if (subscription != null) {
            upsertFeed(subscription)

            AddFeedResult.Success(subscription.title)
        } else {
//            val choices = body.choices.map {
//                FeedOption(feedURL = it.feed_url, title = it.title)
//            }

//            AddFeedResult.MultipleChoices(choices)
        }
    }

    suspend fun refreshAll() {
        refreshFeeds()
        refreshTaggings()
        refreshArticles()
    }

    private suspend fun refreshFeeds() {
        withResult(feedbin.subscriptions()) { subscriptions ->
            subscriptions.forEach { subscription ->
                upsertFeed(subscription)
            }

            val feedsToRemove = subscriptions.map { it.feed_id.toString() }

            database.feedsQueries.deleteAllExcept(feedsToRemove)
        }
    }

    private fun upsertFeed(subscription: Subscription) {
        database.feedsQueries.upsert(
            id = subscription.feed_id.toString(),
            subscription_id = subscription.id.toString(),
            title = subscription.title,
            feed_url = subscription.feed_url,
            site_url = subscription.site_url,
        )
    }

    private suspend fun refreshTaggings() {
        withResult(feedbin.taggings()) { taggings ->
            taggings.forEach { tagging ->
                database.taggingsQueries.upsert(
                    id = tagging.id,
                    feed_id = tagging.feed_id.toString(),
                    name = tagging.name,
                )
            }
        }
    }

    private suspend fun refreshArticles() {
        withResult(feedbin.entries(since = maxUpdatedAt())) { entries ->
            val updatedAt = nowUTCInSeconds()

            entries.forEach { entry ->
                database.transaction {
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
                        updated_at = updatedAt
                    )
                }
            }
        }
    }

    private fun maxUpdatedAt(): String? {
        val max = database.articlesQueries.lastUpdatedAt().executeAsOne().MAX
        max ?: return null

        return max.toDateTimeFromSeconds.toString()
    }
}

fun <T> withResult(response: Response<T>, handler: (result: T) -> Unit) {
    val result = response.body()

    if (!response.isSuccessful || result == null) {
        return
    }

    handler(result)
}
