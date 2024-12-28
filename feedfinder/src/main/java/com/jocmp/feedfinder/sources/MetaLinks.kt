package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.DefaultRequest
import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Element

internal class MetaLinks(
    private val response: Response,
    private val request: Request = DefaultRequest()
) : Source {
    override suspend fun find(): List<Feed> {
        try {
            val document = response.findDocument() ?: return emptyList()

            return coroutineScope {
                document.select("link[rel~=alternate]")
                    .filter { element -> isValidLink(element) }
                    .map { async { createFromURL(url = it.absUrl("href"), fetcher = request) } }
                    .awaitAll()
                    .mapNotNull { it }
            }
        } catch (e: Parser.NotFeedError) {
            return emptyList()
        }
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
