package com.jocmp.capy.accounts.miniflux

import com.jocmp.minifluxclient.Enclosure
import com.jocmp.minifluxclient.Entry
import com.jocmp.minifluxclient.EntryStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MinifluxEnclosureParsingTest {
    @Test
    fun `returns image enclosure URL when available`() {
        val entry = buildEntry(
            enclosures = listOf(
                Enclosure(
                    id = 1,
                    user_id = 1,
                    entry_id = 1,
                    url = "https://example.com/podcast.mp3",
                    mime_type = "audio/mpeg",
                    size = 1024
                ),
                Enclosure(
                    id = 2,
                    user_id = 1,
                    entry_id = 1,
                    url = "https://example.com/photo.jpg",
                    mime_type = "image/jpeg",
                    size = 2048
                )
            )
        )

        assertEquals(
            expected = "https://example.com/photo.jpg",
            actual = MinifluxEnclosureParsing.parsedImageURL(entry)
        )
    }

    @Test
    fun `falls back to first img tag in content`() {
        val content = """
            <p>Some text</p>
            <img src="https://example.com/inline.png" />
            <img src="https://example.com/second.png" />
        """.trimIndent()

        val entry = buildEntry(content = content)

        assertEquals(
            expected = "https://example.com/inline.png",
            actual = MinifluxEnclosureParsing.parsedImageURL(entry)
        )
    }

    @Test
    fun `returns null when no images found`() {
        val entry = buildEntry(content = "<p>No images here</p>")

        assertNull(MinifluxEnclosureParsing.parsedImageURL(entry))
    }

    private fun buildEntry(
        content: String = "",
        enclosures: List<Enclosure>? = null
    ): Entry {
        return Entry(
            id = 1,
            user_id = 1,
            feed_id = 1,
            status = EntryStatus.UNREAD,
            hash = "abc123",
            title = "Test Entry",
            url = "https://example.com/article",
            comments_url = null,
            published_at = "2024-01-01T00:00:00Z",
            created_at = "2024-01-01T00:00:00Z",
            changed_at = "2024-01-01T00:00:00Z",
            content = content,
            author = null,
            share_code = null,
            starred = false,
            reading_time = 1,
            enclosures = enclosures,
            feed = null
        )
    }
}
