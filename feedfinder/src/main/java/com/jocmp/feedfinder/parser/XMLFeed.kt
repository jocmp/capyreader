package com.jocmp.feedfinder.parser

import com.prof18.rssparser.RssParser
import com.prof18.rssparser.exception.RssParsingException
import com.prof18.rssparser.model.RssChannel

internal class XMLFeed(private val channel: RssChannel?) : Feed {
    override fun isValid(): Boolean {
        return channel != null &&
                !channel.link.isNullOrBlank() &&
                !channel.title.isNullOrBlank() &&
                hasEntries()
    }

    private fun hasEntries(): Boolean {
        return channel != null &&
                channel.items.isNotEmpty()
    }

    companion object {
        suspend fun from(body: String): XMLFeed {
            val channel = try {
                RssParser().parse(body)
            } catch (e: RssParsingException) {
                null
            }

            return XMLFeed(channel)
        }
    }
}
