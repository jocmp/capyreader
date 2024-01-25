package com.jocmp.feedfinder.parser

import com.prof18.rssparser.RssParser
import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.model.RssChannel
import java.net.URL

internal class XMLFeed(
    override val feedURL: URL,
    private val channel: RssChannel?
) : Feed {
    override fun isValid(): Boolean {
        return channel != null &&
                !channel.link.isNullOrBlank() &&
                hasEntries()
    }

    override val name: String
        get() = channel!!.title ?: ""

    override val siteURL: URL?
        get() = channel?.link?.let {
            URL(it)
        }

    private fun hasEntries(): Boolean {
        return channel != null &&
                channel.items.isNotEmpty()
    }

    companion object {
        suspend fun from(url: URL, body: String): XMLFeed {
            val channel = try {
                RssParser().parse(body)
            } catch (e: RssParsingException) {
                null
            }

            return XMLFeed(feedURL = url, channel = channel)
        }
    }
}
