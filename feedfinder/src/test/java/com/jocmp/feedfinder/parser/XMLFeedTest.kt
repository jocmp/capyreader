package com.jocmp.feedfinder.parser

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XMLFeedTest {
    @Test
    fun isValid() = runTest {
        val responseBody = File("src/test/resources/arstechnica_feed.xml").readText()

        val feed = XMLFeed.from(url = URL("https://arstechnica.com"), body = responseBody)

        assertTrue(feed.isValid())
    }

    @Test
    fun siteURLIsInvalid() = runTest {
        val responseBody = File("src/test/resources/brasildefato_com_br.xml").readText()

        val feed = XMLFeed.from(
            url = URL("https://www.brasildefato.com.br/rss2.xml"),
            body = responseBody
        )

        assertTrue(feed.isValid())
        assertNull(feed.siteURL)
    }
}
