package com.jocmp.feedfinder.source

import com.jocmp.feedfinder.Feed
import org.jsoup.nodes.Element
import java.net.URL

internal class MetaLink(source: Source) : Source by source {
    override fun find(): List<Feed> {
        if (document == null) {
            return emptyList()
        }

        return document.select("link[rel~=alternate]")
            .filter { element -> isValidLink(element) }
            .map { Feed(feedURL = URL(it.attr("href"))) }
    }

    private fun isValidLink(element: Element): Boolean {
        val type = element.attr("type").lowercase()
        val href = element.attr("href")

        return href.isNotBlank() && linkTypes.contains(type)
    }

    companion object {
        private val linkTypes = setOf(
            "application/rss+xml",
            "application/atom+xml",
            "application/feed+json",
            "application/json"
        )
    }
}
