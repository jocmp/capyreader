package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.TestRequest
import com.jocmp.feedfinder.testFile
import com.jocmp.feedfinder.testResource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetaLinksTest {
    @Test
    fun `it finds a single link`() = runBlocking {
        val feedURL = "http://feeds.arstechnica.com/arstechnica/index"
        val response = Response(
            url = URL("https://arstechnica.com"),
            body = testFile("arstechnica.html").readText()
        )

        val sites = mapOf(
            feedURL to testResource("arstechnica_feed.xml")
        )

        val source = MetaLinks(response, TestRequest(sites))
        val feed = source.find().first()

        assertTrue(feed.isValid())
        assertEquals(expected = URL(feedURL), actual = feed.feedURL)
    }

    @Test
    fun `it works with relative URLs`() = runBlocking {
        val feedURL = "https://theverge.com/rss/index.xml"

        val response = Response(
            url = URL("https://theverge.com"),
            body = testFile("theverge.html").readText()
        )

        val sites = mapOf(
            feedURL to testResource("theverge_feed.xml")
        )

        val source = MetaLinks(response, TestRequest(sites))
        val feed = source.find().first()

        assertTrue(feed.isValid())
        assertEquals(expected = URL(feedURL), actual = feed.feedURL)
    }
}
