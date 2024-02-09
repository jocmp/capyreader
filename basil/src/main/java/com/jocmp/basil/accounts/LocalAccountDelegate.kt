package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import com.jocmp.basil.common.parseISODate
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssItem
import okhttp3.OkHttpClient
import java.net.URL

internal class LocalAccountDelegate(httpClient: OkHttpClient) : AccountDelegate {
    val rssParser = RssParserBuilder(callFactory = httpClient).build()

    override suspend fun createFeed(feedURL: URL): Result<String> {
        return Result.success(feedURL.toString())
    }

    override suspend fun fetchAll(feed: Feed): List<ParsedItem> {
        val result = rssParser.getRssChannel(feed.feedURL)

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
