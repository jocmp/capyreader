package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.TestRequest
import com.jocmp.feedfinder.testResource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class BodyLinksTest {
    val document = """
            <a href="/feed">RSS</a>
            <a href="/xml">RSS</a>
            <a href="/atom">RSS</a>
            <a href="/rss">RSS</a>
        """.trimIndent()

    @Test
    fun `finds candidate links in the document body`() = runBlocking {
        val response = Response(
            url = URL("https://example.com"), body = document
        )

        val sites = mapOf(
            "https://example.com/feed" to testResource("arstechnica_feed.xml"),
            "https://example.com/xml" to testResource("arstechnica_feed.xml"),
            "https://example.com/atom" to testResource("arstechnica_feed.xml"),
            "https://example.com/rss" to testResource("arstechnica_feed.xml"),
        )

        val source = BodyLinks(response, TestRequest(sites))
        assertEquals(expected = 4, source.find().size)
    }

    @Test
    fun `should skip HTML links`() = runBlocking {
        val response = Response(
            url = URL("https://example.com"), body = document
        )

        val sites = mapOf(
            "https://example.com/feed" to testResource("arstechnica_feed.xml"),
            "https://example.com/xml" to testResource("arstechnica_feed.xml"),
            "https://example.com/atom" to testResource("arstechnica_feed.xml"),
            "https://example.com/rss" to testResource("arstechnica.html"),
        )

        val source = BodyLinks(response, TestRequest(sites))
        assertEquals(expected = 3, source.find().size)
    }
}
