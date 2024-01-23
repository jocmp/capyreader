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
import java.net.URL

internal class MetaLinks(
    private val response: Response,
    private val request: Request = DefaultRequest()
) : Source {
    override suspend fun find(): List<Feed> {
        val document = response.findDocument() ?: return emptyList()

        return coroutineScope {
            document.select("link[rel~=alternate]")
                .filter { element -> isValidLink(element) }
                .map { async { request.fetch(url = URL(it.absUrl("href"))) } }
                .awaitAll()
                .mapNotNull { response ->
                    when (val result = response.parse()) {
                        is Parser.Result.ParsedFeed -> result.feed
                        is Parser.Result.HTMLDocument -> null
                    }
                }
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
