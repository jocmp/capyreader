package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.junit.Test
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class ReadingTimeTest {

    @Test
    fun `returns zero when article has no content`() {
        val article = createArticle(contentHTML = "", summary = "")

        assertEquals(0, article.readingTimeMinutes())
    }

    @Test
    fun `rounds up to the nearest minute`() {
        val words = List(50) { "word" }.joinToString(" ")
        val article = createArticle(contentHTML = "<p>$words</p>")

        assertEquals(1, article.readingTimeMinutes())
    }

    @Test
    fun `computes reading time from word count using ~225 wpm`() {
        val words = List(900) { "word" }.joinToString(" ")
        val article = createArticle(contentHTML = "<p>$words</p>")

        assertEquals(4, article.readingTimeMinutes())
    }

    @Test
    fun `strips HTML tags before counting words`() {
        val words = List(225) { "word" }.joinToString(" ")
        val article = createArticle(
            contentHTML = "<div><script>noisy();</script><p>$words</p></div>"
        )

        assertEquals(1, article.readingTimeMinutes())
    }

    @Test
    fun `falls back to summary when contentHTML is blank`() {
        val words = List(225) { "word" }.joinToString(" ")
        val article = createArticle(contentHTML = "", summary = words)

        assertEquals(1, article.readingTimeMinutes())
    }

    private fun createArticle(
        contentHTML: String = "<p>Content</p>",
        summary: String = "Summary",
    ): Article {
        return Article(
            id = "1",
            feedID = "feed-1",
            title = "Test",
            author = null,
            contentHTML = contentHTML,
            url = URL("https://example.com/article"),
            summary = summary,
            imageURL = null,
            updatedAt = ZonedDateTime.now(),
            publishedAt = ZonedDateTime.now(),
            read = false,
            starred = false,
        )
    }
}
