package com.jocmp.readerclient

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SubscriptionTest {
    @Test
    fun `feedURL prefers explicit url`() {
        val sub = Subscription(
            id = "feed/http://example.com/rss",
            title = "Example",
            categories = emptyList(),
            url = "http://feeds.example.com/rss",
            htmlUrl = "http://example.com",
            iconUrl = null,
            frssPriority = null,
        )

        assertEquals(expected = "http://feeds.example.com/rss", actual = sub.feedURL)
    }

    @Test
    fun `feedURL falls back to id when url is missing`() {
        val sub = Subscription(
            id = "feed/http://feeds.propublica.org/propublica/main",
            title = "ProPublica",
            categories = emptyList(),
            url = null,
            htmlUrl = "https://www.propublica.org/",
            iconUrl = null,
            frssPriority = null,
        )

        assertEquals(
            expected = "http://feeds.propublica.org/propublica/main",
            actual = sub.feedURL,
        )
    }
}
