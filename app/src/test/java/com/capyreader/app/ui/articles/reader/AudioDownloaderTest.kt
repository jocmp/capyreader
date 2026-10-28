package com.capyreader.app.ui.articles.reader

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioDownloaderTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `fileName uses the title with the URL extension`() {
        val name = AudioDownloader.fileName(
            url = "https://chrt.fm/track/abc/npr.simplecastaudio.com/default.mp3?awCollectionId=510289",
            title = "How surveillance crept up on us",
            mimeType = "audio/mpeg",
        )

        assertEquals("How surveillance crept up on us.mp3", name)
    }

    @Test
    fun `fileName strips path separators from the title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode.mp3",
            title = "Q&A: AC/DC? \"Live\"",
            mimeType = null,
        )

        assertEquals("Q&A AC DC Live.mp3", name)
    }

    @Test
    fun `fileName falls back to the URL without a title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode-42.mp3",
            title = null,
            mimeType = null,
        )

        assertEquals("episode-42.mp3", name)
    }

    @Test
    fun `fileName falls back to the URL with a blank title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode-42.mp3",
            title = " / ",
            mimeType = null,
        )

        assertEquals("episode-42.mp3", name)
    }

    @Test
    fun `fileName uses the mime type when the URL has no extension`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/play/12345",
            title = "Episode",
            mimeType = "audio/mpeg",
        )

        assertEquals("Episode.mp3", name)
    }

    @Test
    fun `fileName strips leading dots`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode.mp3",
            title = "...And Justice for All",
            mimeType = null,
        )

        assertEquals("And Justice for All.mp3", name)
    }

    @Test
    fun `fileName limits multibyte titles by bytes`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode.mp3",
            title = "播客".repeat(100),
            mimeType = null,
        )

        assertEquals(66, name.removeSuffix(".mp3").length)
        assertTrue(name.toByteArray(Charsets.UTF_8).size <= 255)
    }
}
