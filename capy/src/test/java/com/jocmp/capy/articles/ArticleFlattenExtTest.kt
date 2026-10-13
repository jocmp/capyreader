package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.Enclosure
import com.jocmp.mallet.LinearVideo
import java.net.URL
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleFlattenExtTest {
    private val videoURL = "https://media.mstdn.social/media_attachments/files/117/429/292/943/403/835/original/dcf435c7bacf9846.mp4"

    private val videoEnclosure = Enclosure(
        url = URL(videoURL),
        type = "video/mp4",
        itunesDurationSeconds = null,
        itunesImage = null,
    )

    @Test
    fun `video enclosure already in content is skipped`() {
        val article = article(
            contentHTML = """
                <p>A Russian 2S19 Msta-S self-propelled howitzer, destroyed.</p>
                <div class="enclosure"><p class="enclosure-content"><video preload="none" src="$videoURL" controls="controls"></video></p></div>
            """.trimIndent(),
            enclosures = listOf(videoEnclosure),
        )

        val videos = article.flatten().elements.filterIsInstance<LinearVideo>()

        assertEquals(listOf(videoURL), videos.map { it.firstSource.uri })
    }

    @Test
    fun `video enclosure missing from content is appended`() {
        val article = article(
            contentHTML = "<p>A Russian 2S19 Msta-S self-propelled howitzer, destroyed.</p>",
            enclosures = listOf(videoEnclosure),
        )

        val videos = article.flatten().elements.filterIsInstance<LinearVideo>()

        assertEquals(listOf(videoURL), videos.map { it.firstSource.uri })
    }

    private fun article(contentHTML: String, enclosures: List<Enclosure>): Article {
        val now = ZonedDateTime.now()

        return Article(
            id = "1",
            feedID = "1",
            title = "",
            author = null,
            contentHTML = contentHTML,
            url = URL("https://mstdn.social/@noelreports/117429294664991238"),
            summary = "",
            imageURL = null,
            updatedAt = now,
            publishedAt = now,
            read = false,
            starred = false,
            enclosures = enclosures,
        )
    }
}
