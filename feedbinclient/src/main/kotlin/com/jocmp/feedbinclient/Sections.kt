package com.jocmp.feedbinclient

import android.icu.text.CaseMap.Fold
import com.jocmp.feedbinclient.api.FeedbinClient
import com.jocmp.feedbinclient.api.FeedbinSubscription
import com.jocmp.feedbinclient.api.FeedbinTagging
import com.jocmp.feedbinclient.common.request
import com.jocmp.feedbinclient.db.FeedbinDatabase
import retrofit2.Response

interface Sections {
    suspend fun all(): Result<List<Section>>
}

class DefaultSections(
    private val database: FeedbinDatabase,
    private val client: FeedbinClient,
) : Sections {
    override suspend fun all(): Result<List<Section>> {
        val sections = allSections()

        if (sections.isNotEmpty()) {
            return Result.success(sections)
        }

        request { client.subscriptions() }.onSuccess { syncSubscriptions(it) }
        request { client.taggings() }.onSuccess { syncTaggings(it) }

        return Result.success(allSections())
    }

    private fun syncSubscriptions(response: Response<List<FeedbinSubscription>>) {
        val feedbinSubscriptions = response.body().orEmpty()

        database.transaction {
            feedbinSubscriptions.forEach { subscription ->
                subscriptions.insert(
                    id = subscription.id.toLong(),
                    created_at = subscription.created_at,
                    title = subscription.title,
                    feed_id = subscription.feed_id.toLong(),
                    feed_url = subscription.feed_url,
                    site_url = subscription.site_url
                )
            }
        }
    }

    private fun syncTaggings(response: Response<List<FeedbinTagging>>) {
        val feedbinTaggings = response.body().orEmpty()

        database.transaction {
            feedbinTaggings.forEach { tagging ->
                taggings.insert(
                    id = tagging.id.toLong(),
                    feed_id = tagging.feed_id.toLong(),
                    name = tagging.name
                )
            }
        }
    }

    private fun allSections(): List<Section> {
        val nestedFeeds = mutableMapOf<String, MutableList<Feed>>()
        val topLevelFeeds = mutableListOf<Feed>()

        subscriptions.findTaggedSubscriptions { title,
                                                feed_id,
                                                tagging_name ->
            val feed = Feed(id = feed_id, title = title)

            if (tagging_name.isNullOrBlank()) {
                topLevelFeeds.add(feed)
            } else {
                if (nestedFeeds[tagging_name] == null) {
                    nestedFeeds[tagging_name] = mutableListOf(feed)
                } else {
                    nestedFeeds[tagging_name]!!.add(feed)
                }
            }
        }.executeAsList()

        val sections = mutableListOf<Section>()

        nestedFeeds.toSortedMap().forEach { (name, feeds) ->
            sections.add(Section.FolderSection(Folder(name = name, feeds = feeds)))
        }

        sections.add(Section.FeedSection(feeds = topLevelFeeds.sortedBy { it.title }))

        return sections
    }

    private val subscriptions: SubscriptionQueries
        get() = database.subscriptionQueries

    private val taggings: TaggingQueries
        get() = database.taggingQueries
}
