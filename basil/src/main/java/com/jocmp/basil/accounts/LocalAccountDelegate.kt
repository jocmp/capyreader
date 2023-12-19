package com.jocmp.basil.accounts

import com.jocmp.basil.Account
import com.jocmp.basil.Feed
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.UUID

internal class LocalAccountDelegate(private val account: Account) : AccountDelegate {
    override suspend fun createFeed(feedURL: URL): ExternalFeed {
        val allFeeds = mutableSetOf<Feed>().apply {
            addAll(account.feeds)
            addAll(account.folders.flatMap { it.feeds })
        }

        val existingFeed = allFeeds.find { it.feedURL == feedURL.toString() }

        if (existingFeed != null) {
            return ExternalFeed(externalID = existingFeed.externalID)
        }

        return ExternalFeed(externalID = feedURL.toString())
    }

    override suspend fun fetchAll(feed: Feed): List<ParsedItem> {
        val result = RssParser().getRssChannel(feed.feedURL)

        return result.items
            .filter { it.isIdentifiable }
            .map { item ->
                ParsedItem(
                    externalID = item.guid ?: item.link!!,
                    title = item.title,
                    contentHTML = item.content,
                    url = item.link,
                    summary = item.description,
                    imageURL = item.image
                )
            }
    }
}

private val RssItem.isIdentifiable: Boolean
    get() = !(guid.isNullOrBlank() || link.isNullOrBlank())
