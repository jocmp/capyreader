package com.jocmp.feedfinder.parser

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.net.URL
import kotlin.test.assertTrue

class XMLFeedTest {
    @Test
    fun isValid() = runBlocking {
        val responseBody = File("src/test/resources/arstechnica_feed.xml").readText()

        val feed = XMLFeed.from(url = URL("https://arstechnica.com"), body = responseBody)

        assertTrue(feed.isValid())
    }
}
