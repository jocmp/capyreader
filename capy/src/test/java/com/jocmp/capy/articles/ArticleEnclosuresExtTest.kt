package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.Enclosure
import org.junit.Test
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertContains

class ArticleEnclosuresExtTest {

    @Test
    fun `audioEnclosureHTML includes audio enclosures`() {
        val audioEnclosure = Enclosure(
            url = URL("https://example.com/podcast.mp3"),
            type = "audio/mpeg",
            itunesDurationSeconds = 3600L,
            itunesImage = "https://example.com/cover.jpg"
        )

        val article = createArticle(
            enclosures = listOf(audioEnclosure)
        )

        val html = article.audioEnclosureHTML()

        assertContains(html, "audio-enclosure")
        assertContains(html, "https://example.com/podcast.mp3")
        assertContains(html, "1:00:00")
    }

    @Test
    fun `audioEnclosureHTML formats duration correctly for minutes`() {
        val audioEnclosure = Enclosure(
            url = URL("https://example.com/podcast.mp3"),
            type = "audio/mpeg",
            itunesDurationSeconds = 125L,
            itunesImage = null
        )

        val article = createArticle(
            enclosures = listOf(audioEnclosure)
        )

        val html = article.audioEnclosureHTML()

        assertContains(html, "2:05")
    }

    @Test
    fun `enclosureHTML excludes non-audio enclosures from audio section`() {
        val videoEnclosure = Enclosure(
            url = URL("https://example.com/video.mp4"),
            type = "video/mp4",
            itunesDurationSeconds = null,
            itunesImage = null
        )

        val article = createArticle(
            enclosures = listOf(videoEnclosure)
        )

        val html = article.enclosureHTML()

        assertContains(html, "video")
        assertEquals(false, html.contains("audio-enclosure"))
    }

    @Test
    fun `audioEnclosureHTML escapes special characters in title`() {
        val audioEnclosure = Enclosure(
            url = URL("https://example.com/podcast.mp3"),
            type = "audio/mpeg",
            itunesDurationSeconds = 60L,
            itunesImage = null
        )

        val article = createArticle(
            title = "Test's \"Episode\"",
            enclosures = listOf(audioEnclosure)
        )

        val html = article.audioEnclosureHTML()

        // JS-escaped in onclick, HTML-escaped in content
        assertContains(html, "Test\\'s")
        assertContains(html, "&quot;Episode&quot;")
    }

    private fun createArticle(
        title: String = "Test Article",
        feedName: String = "Test Feed",
        enclosures: List<Enclosure> = emptyList(),
    ): Article {
        return Article(
            id = "1",
            feedID = "feed-1",
            title = title,
            author = null,
            contentHTML = "<p>Content</p>",
            url = URL("https://example.com/article"),
            summary = "Summary",
            imageURL = null,
            updatedAt = ZonedDateTime.now(),
            publishedAt = ZonedDateTime.now(),
            read = false,
            starred = false,
            feedName = feedName,
            enclosures = enclosures,
        )
    }
}
