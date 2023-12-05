package com.jocmp.feedfinder.parser

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class XMLFeedTest {
    @Test
    fun isValid() = runBlocking {
        val responseBody = File("src/test/resources/arstechnica_feed.xml").readText()

        val feed = XMLFeed.from(responseBody)

        assertTrue(feed.isValid())
    }
}
