package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.DefaultRequest
import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import org.jsoup.nodes.Document

internal class KnownPatterns(
    private val response: Response,
    private val request: Request = DefaultRequest()
) : Source {
    override suspend fun find(): List<Feed> {
        val url = response.url.toString()

        val result = urls.firstNotNullOfOrNull {
            val match = it.regex.find(url)?.groupValues?.getOrNull(1)

            if (match != null) {
                Pair(it, match)
            } else {
                null
            }
        }

        try {
            if (result != null) {
                val (match, matchResult) = result
                val feedURL = match.template.format(matchResult)

                val feed = createFromURL(feedURL, fetcher = request)

                if (feed != null) {
                    return listOf(feed)
                }
            }

            val document = response.findDocument()

            if (document != null && isYouTubeDomain) {
                val channelId = findChannelID(document)
                val feedURL = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
                val feed = createFromURL(feedURL, fetcher = request)

                if (feed != null) {
                    return listOf(feed)
                }
            }

            if (isMastodonServer) {
                val feedURL = "${response.url}.rss"
                val feed = createFromURL(feedURL, fetcher = request)

                if (feed != null) {
                    return listOf(feed)
                }
            }

            return emptyList()
        } catch (e: Parser.NotFeedError) {
            return emptyList()
        }
    }

    private fun findChannelID(document: Document): String? {
        return document.selectFirst("meta[itemprop='identifier']")?.attr("content")
    }

    private val isYouTubeDomain: Boolean
        get() = response.url.toString().startsWith("https://www.youtube.com")

    private val isMastodonServer: Boolean
        get() = response.headers.any { (_, values) ->
            values.any { it.contains("mastodon", ignoreCase = true) }
        }

    private data class URLTemplate(val template: String, val regex: Regex)

    companion object {
        private val urls = listOf(
            URLTemplate(
                template = "https://www.youtube.com/feeds/videos.xml?channel_id=%s",
                regex = Regex("https://www\\.youtube\\.com/channel/([^/#?]*)")
            ),
            URLTemplate(
                template = "https://www.youtube.com/feeds/videos.xml?user=%s",
                regex = Regex("https://www\\.youtube\\.com/user/([^/#?]*)")
            ),
            URLTemplate(
                template = "https://www.youtube.com/feeds/videos.xml?playlist_id=%s",
                regex = Regex("https://www\\.youtube\\.com/playlist\\?list=([^&]*)")
            ),
            URLTemplate(
                template = "https://www.reddit.com/r/%s.rss",
                regex = Regex("https?://(?:www\\.)?reddit\\.com/r/([^/#?]*)")
            ),
            URLTemplate(
                template = "https://vimeo.com/%s/videos/rss",
                regex = Regex("https://vimeo\\.com/([^/#?]*)")
            ),
            URLTemplate(
                template = "https://github.com/%s.atom",
                regex = Regex("https://github\\.com/(orgs/[^/#?]+/discussions)")
            ),
            URLTemplate(
                template = "https://github.com/%s.atom",
                regex = Regex("https://github\\.com/([^/#?]+/[^/#?]+/discussions)")
            )
        )
    }
}
