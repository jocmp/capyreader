package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import com.jocmp.basil.common.nowUTCInSeconds
import com.jocmp.basil.common.toDateTime
import com.jocmp.basil.db.Database
import com.jocmp.feedbinclient.Feedbin
import retrofit2.Response
import java.util.Date
import java.util.GregorianCalendar

internal class FeedbinAccountDelegate(
    val database: Database,
    private val feedbin: Feedbin,
) {
    fun fetchAll(feed: Feed): List<ParsedItem> {
        return emptyList()
    }

    suspend fun refreshAll() {
        refreshFeeds()
        refreshTaggings()
        refreshArticles()
    }

    private suspend fun refreshFeeds() {
        withResult(feedbin.subscriptions()) { subscriptions ->
            subscriptions.forEach { subscription ->
                database.feedsQueries.upsert(
                    id = subscription.feed_id.toString(),
                    subscription_id = subscription.id.toString(),
                    feed_url = subscription.feed_url,
                    site_url = subscription.site_url,
                )
            }

            val feedsToRemove = subscriptions.map { it.feed_id.toString() }

            database.feedsQueries.deleteAllExcept(feedsToRemove)
        }
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
        withResult(feedbin.entries(since = maxArrivedAt())) { entries ->
            val arrivedAt = nowUTCInSeconds()

            entries.forEach { entry ->
                database.transaction {
                    database.articlesQueries.create(
                        id = entry.id.toString(),
                        feed_id = entry.id.toString(),
                        title = entry.title,
                        content_html = entry.content,
                        url = entry.url,
                        summary = entry.summary,
                        image_url = entry.images?.original_url,
                        published_at = entry.published.toDateTime?.toEpochSecond(),
                    )

                    database.articlesQueries.updateStatus(
                        article_id = entry.id.toString(),
                        arrived_at = arrivedAt
                    )
                }
            }
        }
    }

    private fun maxArrivedAt(): String? {
        val result = database.articlesQueries.lastArrivalTime().executeAsOne()

        return result.MAX?.toDateTime?.toString()
    }
}

fun <T> withResult(response: Response<T>, handler: (result: T) -> Unit) {
    val result = response.body()

    if (!response.isSuccessful || result == null) {
        return
    }

    handler(result)
}
