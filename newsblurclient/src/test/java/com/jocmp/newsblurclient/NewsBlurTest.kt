package com.jocmp.newsblurclient

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewsBlurTest {
    private lateinit var server: MockWebServer
    private lateinit var newsBlur: NewsBlur

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        newsBlur = NewsBlur.create(client = OkHttpClient(), baseURL = baseURL)
    }

    @After
    fun teardown() {
        server.close()
    }

    @Test
    fun `login returns the session cookie`() = runTest {
        server.enqueue(
            MockResponse(
                headers = Headers.headersOf(
                    "Set-Cookie", "newsblur_sessionid=abc123; Path=/; HttpOnly",
                ),
                body = """{"code": 1, "errors": null, "result": "ok", "authenticated": true, "user_id": 1}""",
            )
        )

        val result = NewsBlur.login(username = "capy", password = "secret", baseURL = baseURL)

        assertEquals("abc123", result.getOrNull())
        assertEquals("username=capy&password=secret", server.takeRequest().body?.utf8())
    }

    @Test
    fun `login surfaces the server error`() = runTest {
        server.enqueue(
            MockResponse(
                body = """{"code": -1, "errors": {"__all__": ["That username is not registered. Please try again."]}, "result": "ok", "authenticated": false}""",
            )
        )

        val result = NewsBlur.login(username = "capy", password = "secret", baseURL = baseURL)

        assertEquals("That username is not registered. Please try again.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails on an invalid URL`() = runTest {
        val result = NewsBlur.login(username = "capy", password = "secret", baseURL = "https://news blur.example.org/")

        assertTrue(result.isFailure)
    }

    @Test
    fun feeds() = runTest {
        server.enqueue(MockResponse(body = fixture("feeds.json")))

        val body = newsBlur.feeds().body()!!

        val feed = body.feeds.getValue("9451748")
        assertEquals("9to5Google", feed.feed_title)
        assertEquals("https://9to5google.com/feed/", feed.feed_address)
        assertEquals(listOf(9451748L), body.flat_folders.getValue("Tech"))
        assertEquals("/reader/feeds?flat=true&update_counts=false", server.takeRequest().target)
    }

    @Test
    fun unreadStoryHashes() = runTest {
        server.enqueue(MockResponse(body = fixture("unread_story_hashes.json")))

        val body = newsBlur.unreadStoryHashes().body()!!

        assertTrue(body.authenticated)
        assertEquals(3, body.unread_feed_story_hashes.getValue("9451748").size)
    }

    @Test
    fun starredStoryHashes() = runTest {
        server.enqueue(MockResponse(body = fixture("starred_story_hashes.json")))

        val body = newsBlur.starredStoryHashes().body()!!

        assertEquals(listOf("9451748:a6f371"), body.starred_story_hashes)
    }

    @Test
    fun riverStories() = runTest {
        server.enqueue(MockResponse(body = fixture("river_stories.json")))

        val stories = newsBlur.riverStories(listOf("9451748:a6f371", "9451748:8578ca")).body()!!.stories

        val story = stories.first()
        assertEquals("9451748:a6f371", story.story_hash)
        assertEquals(9451748L, story.story_feed_id)
        assertEquals("1793194228", story.story_timestamp)
        assertTrue(story.starred)
        assertEquals(0, story.read_status)
        assertEquals(
            "/reader/river_stories?h=9451748%3Aa6f371&h=9451748%3A8578ca&include_hidden=false",
            server.takeRequest().target
        )
    }

    @Test
    fun `riverStories defaults starred to false`() = runTest {
        server.enqueue(
            MockResponse(
                body = """{"stories": [{"story_hash": "1:a", "story_feed_id": 1, "story_title": "Title", "story_timestamp": "1"}]}"""
            )
        )

        val story = newsBlur.riverStories(listOf("1:a")).body()!!.stories.first()

        assertFalse(story.starred)
    }

    @Test
    fun markStoryHashesAsRead() = runTest {
        server.enqueue(MockResponse(body = """{"code": 1}"""))

        newsBlur.markStoryHashesAsRead(listOf("1:a", "1:b"))

        assertEquals("story_hash=1%3Aa&story_hash=1%3Ab", server.takeRequest().body?.utf8())
    }

    @Test
    fun moveFeedToFolders() = runTest {
        server.enqueue(MockResponse(body = """{"code": 1}"""))

        newsBlur.moveFeedToFolders(feedID = 1, inFolders = listOf(""), toFolders = listOf("Tech", "News"))

        assertEquals(
            "feed_id=1&in_folders=&to_folders=Tech&to_folders=News",
            server.takeRequest().body?.utf8()
        )
    }

    private val baseURL
        get() = server.url("/").toString()

    private fun fixture(name: String): String {
        return javaClass.classLoader!!.getResource(name)!!.readText()
    }
}
