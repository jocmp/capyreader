package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.testFile
import com.jocmp.feedfinder.testResource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.net.URL
import kotlin.test.assertTrue

class MetaLinkSourceTest {
    @Test
    fun `it finds a single link`() = runBlocking {
        val response = Response(
            body = testFile("arstechnica.html").readText()
        )

        val sites = mapOf(
            "http://feeds.arstechnica.com/arstechnica/index" to testResource("arstechnica_feed.xml")
        )

        val source = MetaLinkSource(response, TestRequest(sites))
        val feed = source.find().first()

        assertTrue(feed.isValid())
    }
}

private class TestRequest(val sites: Map<String, String>) : Request {
    override suspend fun fetch(url: URL): Response {
        val body = File(sites[url.toString()]!!).readText()
        return Response(body = body)
    }
}
