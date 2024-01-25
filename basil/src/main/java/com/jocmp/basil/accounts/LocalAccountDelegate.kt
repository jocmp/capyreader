package com.jocmp.basil.accounts

import com.jocmp.basil.Account
import com.jocmp.basil.Feed
import com.jocmp.basil.shared.parseISODate
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssItem
import java.net.URL

internal class LocalAccountDelegate(private val account: Account) : AccountDelegate {
    override suspend fun createFeed(feedURL: URL): Result<String> {
        return Result.success(feedURL.toString())
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
                    imageURL = item.image,
                    publishedAt = parseISODate(item.pubDate)
                )
            }
    }
}

private val RssItem.isIdentifiable: Boolean
    get() = !(guid.isNullOrBlank() || link.isNullOrBlank())
