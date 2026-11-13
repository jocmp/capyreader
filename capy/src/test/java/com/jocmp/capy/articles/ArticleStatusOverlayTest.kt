package com.jocmp.capy.articles

import com.jocmp.capy.Article
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ArticleStatusOverlayTest {
    @Test
    fun `read and starred changes merge into one override`() {
        val overlay = ArticleStatusOverlay()

        overlay.apply(StatusChange(listOf("a"), read = true))
        overlay.apply(StatusChange(listOf("a"), starred = true))

        val override = overlay.overrides.value.getValue("a")
        assertEquals(true, override.read)
        assertEquals(true, override.starred)
    }

    @Test
    fun `clearBefore drops older overrides for the loaded rows only`() {
        val overlay = ArticleStatusOverlay()
        overlay.apply(StatusChange(listOf("loaded", "elsewhere"), read = true))

        val loadStartedAt = ArticleStatusOverlay.now()
        overlay.apply(StatusChange(listOf("newer"), read = true))

        overlay.clearBefore(loadStartedAt, listOf("loaded", "newer"))

        assertNull(overlay.overrides.value["loaded"])
        assertTrue(overlay.overrides.value.containsKey("elsewhere"))
        assertTrue(overlay.overrides.value.containsKey("newer"))
    }

    @Test
    fun `retainOnly drops overrides outside the list`() {
        val overlay = ArticleStatusOverlay()
        overlay.apply(StatusChange(listOf("listed", "gone"), read = true))

        overlay.retainOnly(setOf("listed"))

        assertEquals(setOf("listed"), overlay.overrides.value.keys)
    }

    @Test
    fun `withStatus returns the same article when nothing changes`() {
        val article = article(read = true, starred = false)

        assertSame(article, article.withStatus(null))
        assertSame(article, article.withStatus(StatusOverride(read = true)))
    }

    @Test
    fun `withStatus applies the override`() {
        val article = article(read = false, starred = false)

        val updated = article.withStatus(StatusOverride(read = true, starred = true))

        assertEquals(true, updated.read)
        assertEquals(true, updated.starred)
    }

    private fun article(read: Boolean, starred: Boolean): Article {
        val now = ZonedDateTime.now()

        return Article(
            id = "a",
            feedID = "feed",
            title = "Title",
            author = null,
            contentHTML = "",
            url = null,
            summary = "",
            imageURL = null,
            updatedAt = now,
            publishedAt = now,
            read = read,
            starred = starred,
        )
    }
}
