package com.jocmp.capy.accounts.local

import com.jocmp.capy.rssItemFixture
import com.jocmp.rssparser.model.Media
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RichMediaTest {
    private val media = Media(
        title = "My Video Title",
        contentUrl = "https://example.com",
        description = "Description here",
        thumbnailUrl = "https://example.com/test.jpg",
    )

    @Test
    fun parse() {
        val item = rssItemFixture(
            youtubeVideoID = "video123",
            media = media,
        )

        val parsed = RichMedia.parse(item)

        val expectedHTML = """
           <div>
             <iframe src="https://www.youtube.com/embed/video123" />
             <p>Description here</p>
           </div>
        """.trimIndent()

        assertEquals(expected = expectedHTML, actual = parsed)
    }

    @Test
    fun parse_missingYouTubeID() {
        val item = rssItemFixture(youtubeVideoID = null, media = media)
        val parsed = RichMedia.parse(item)

        assertNull(parsed)
    }

    @Test
    fun parse_missingMedia() {
        val item = rssItemFixture(media = null)
        val parsed = RichMedia.parse(item)

        assertNull(parsed)
    }
}
