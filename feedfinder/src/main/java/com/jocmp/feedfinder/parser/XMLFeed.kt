package com.jocmp.feedfinder.parser

import com.jocmp.feedfinder.optionalURL
import com.jocmp.rssparser.RssParser
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssItem
import java.net.URL

internal class XMLFeed(
    override val feedURL: URL,
    private val channel: RssChannel?
) : Feed {
    override fun isValid(): Boolean {
        return channel != null &&
                hasEntries()
    }

    override val name: String
        get() = channel!!.title ?: ""

    override val siteURL: URL?
        get() = channel?.link?.let {
            optionalURL(it)
        }
    override val faviconURL: URL?
        get() = channel?.image?.url?.let { optionalURL(it) }

    override val items: List<RssItem>
        get() = channel?.items.orEmpty()

    private fun hasEntries(): Boolean {
        return channel != null &&
                channel.items.isNotEmpty()
    }

    companion object {
        suspend fun from(url: URL, body: String): XMLFeed {
            val channel = try {
                RssParser().parse(body)
            } catch (e: Exception) {
                null
            }

            return XMLFeed(feedURL = url, channel = channel)
        }
    }
}
