package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.DefaultRequest
import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import com.jocmp.feedfinder.toParsedFeed
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Element
import java.net.URL

internal class BodyLinks(
    private val response: Response,
    private val request: Request = DefaultRequest()
) : Source {
    override suspend fun find(): List<Feed> {
        val document = response.findDocument() ?: return emptyList()

        return coroutineScope {
            document.select("a")
                .filter { element -> isCandidate(element) }
                .map { async { request.fetch(url = URL(it.absUrl("href"))) } }
                .awaitAll()
                .mapNotNull { it.toParsedFeed() }
        }
    }

    private fun isCandidate(anchor: Element): Boolean {
        val href = anchor.attr("href")
        return href.isNotBlank() &&
                TYPES.any { type -> href.contains(type) }
    }

    companion object {
        private val TYPES = listOf("feed", "xml", "rss", "atom")
    }
}
