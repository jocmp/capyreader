package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.TestRequest
import com.jocmp.feedfinder.testResource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class GuessTest {
    @Test
    fun `should guess feed`() = runBlocking {
        val response = Response(url = URL("https://example.com"), body = "")

        val sites = mapOf(
            "https://example.com/feed" to testResource("arstechnica_feed.xml")
        )

        val source = Guess(response, TestRequest(sites))

        val feeds = source.find()
        val feedURL = feeds.firstOrNull()?.feedURL

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = URL("https://example.com/feed"), actual = feedURL)
    }

    @Test
    fun `should guess rss`() = runBlocking {
        val response = Response(url = URL("https://example.com"), body = "")

        val sites = mapOf(
            "https://example.com/rss" to testResource("arstechnica_feed.xml")
        )

        val source = Guess(response, TestRequest(sites))

        val feeds = source.find()
        val feedURL = feeds.firstOrNull()?.feedURL

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = URL("https://example.com/rss"), actual = feedURL)
    }
}
