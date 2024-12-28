package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.TestRequest
import com.jocmp.feedfinder.testResource
import kotlinx.coroutines.test.runTest
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class KnownPatternTest {
    @Test
    fun `should recognize known patterns`() = runTest {
        val urls = mapOf(
            "https://www.youtube.com/channel/abc" to "https://www.youtube.com/feeds/videos.xml?channel_id=abc",
            "https://www.youtube.com/user/abc" to "https://www.youtube.com/feeds/videos.xml?user=abc",
            "https://www.youtube.com/playlist?list=abc" to "https://www.youtube.com/feeds/videos.xml?playlist_id=abc",
            "https://www.reddit.com/r/abc" to "https://www.reddit.com/r/abc.rss",
            "https://vimeo.com/abc" to "https://vimeo.com/abc/videos/rss",

            )

        urls.forEach { (knownPattern, destination) ->
            val sites = mapOf(
                knownPattern to testResource("index.html"),
                destination to testResource("feed.xml"),
            )

            val response = Response(
                url = URI(knownPattern).toURL(),
                body = testResource("index.html"),
                charset = null,
            )
            val source = KnownPatterns(response, TestRequest(sites))
            val feeds = source.find()

            assertEquals(expected = 1, actual = feeds.size)
        }
    }

    @Test
    fun `should find YouTube channelId`() = runTest {
        val url = "https://www.youtube.com"
        val channelId = "abc"
        val destination = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
        val documentResult = """
          <!DOCTYPE html>
          <html>
          <head>
              <meta itemprop="identifier" content="$channelId">
          </head>
          </html>
        """.trimIndent()

        val sites = mapOf(
            destination to testResource("feed.xml"),
        )

        val response = Response(
            url = URI(url).toURL(),
            body = documentResult,
            charset = null,
        )
        val source = KnownPatterns(response, TestRequest(sites))
        val feeds = source.find()

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = destination, feeds.first().feedURL.toString())
    }
}
