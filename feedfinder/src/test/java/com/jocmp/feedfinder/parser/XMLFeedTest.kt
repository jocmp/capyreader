package com.jocmp.feedfinder.parser

import kotlinx.coroutines.test.runTest
import java.io.File
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XMLFeedTest {
    @Test
    fun isValid() = runTest {
        val responseBody = File("src/test/resources/arstechnica_feed.xml").readText()
        val feed = XMLFeed.from(url = URI("https://arstechnica.com").toURL(), body = responseBody, charset = null)

        assertTrue(feed.isValid())
    }

    @Test
    fun siteURLIsMissing() = runTest {
        val responseBody = File("src/test/resources/microsoft_support.xml").readText()
        val feed = XMLFeed.from(
            url = URI("https://support.microsoft.com/en-us/feed/rss/6ae59d69-36fc-8e4d-23dd-631d98bf74a9").toURL(),
            body = responseBody,
            charset = null,
        )

        assertTrue(feed.isValid())
        assertNull(feed.siteURL)
    }

    @Test
    fun siteURLIsInvalid() = runTest {
        val responseBody = File("src/test/resources/brasildefato_com_br.xml").readText()
        val feed = XMLFeed.from(
            url = URI("https://www.brasildefato.com.br/rss2.xml").toURL(),
            body = responseBody,
            charset = null,
        )

        assertTrue(feed.isValid())
        assertNull(feed.siteURL)
    }
}
