package com.jocmp.capy.accounts

import com.jocmp.capy.Feed
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mock.ClasspathResources
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FaviconFinderTest {
    private val baseURL = "https://example.com"
    private lateinit var faviconFinder: FaviconFinder

    @Before
    fun setup() {
        faviconFinder = FaviconFinder(OkHttpClient(), AcceptAllFavicons())
    }

    @Test
    fun parse_returnsFirstIconLink() = runTest {
        val html = readResource("favicon_apple_touch_icon.html")

        val result = faviconFinder.parse(html, baseURL)

        assertEquals("https://example.com/apple-touch-icon.png", result)
    }

    @Test
    fun parse_returnsShortcutIcon() = runTest {
        val html = readResource("favicon_shortcut_icon.html")

        val result = faviconFinder.parse(html, baseURL)

        assertEquals("https://example.com/images/favicon.ico", result)
    }

    @Test
    fun parse_fallsBackToFaviconIco() = runTest {
        val html = readResource("favicon_none.html")

        val result = faviconFinder.parse(html, baseURL)

        assertEquals("https://example.com/favicon.ico", result)
    }

    @Test
    fun parse_skipsInvalidURLs() = runTest {
        val html = readResource("favicon_apple_touch_icon.html")
        val rejectFirstFavicon = object : FaviconFetcher {
            private var callCount = 0
            override suspend fun isValid(url: String?): Boolean {
                callCount++
                return callCount > 1
            }
        }
        val finder = FaviconFinder(OkHttpClient(), rejectFirstFavicon)

        val result = finder.parse(html, baseURL)

        assertEquals("https://example.com/favicon-32x32.png", result)
    }

    @Test
    fun siteURL_returnsSiteURL() {
        val feed = Feed(
            id = "1",
            subscriptionID = "1",
            title = "Test Feed",
            feedURL = "https://example.com/feed.xml",
            siteURL = "https://example.com"
        )

        val result = FaviconFinder.siteURL(feed)

        assertEquals("https://example.com", result?.toString())
    }

    @Test
    fun siteURL_extractsFromFeedURL() {
        val feed = Feed(
            id = "1",
            subscriptionID = "1",
            title = "Test Feed",
            feedURL = "https://example.com/feed.xml",
            siteURL = ""
        )

        val result = FaviconFinder.siteURL(feed)

        assertEquals("https://example.com", result?.toString())
    }

    @Test
    fun siteURL_returnsNullWhenNoURL() {
        val feed = Feed(
            id = "1",
            subscriptionID = "1",
            title = "Test Feed",
            feedURL = "",
            siteURL = ""
        )

        val result = FaviconFinder.siteURL(feed)

        assertNull(result)
    }

    private fun readResource(name: String): String {
        return ClasspathResources.resource(name).bufferedReader().readText()
    }

    private class AcceptAllFavicons : FaviconFetcher {
        override suspend fun isValid(url: String?) = true
    }
}
