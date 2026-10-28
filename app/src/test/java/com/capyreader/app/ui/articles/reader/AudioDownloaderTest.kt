package com.capyreader.app.ui.articles.reader

import org.junit.After
import org.junit.Assert.assertEquals
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
        )

        assertEquals("How surveillance crept up on us.mp3", name)
    }

    @Test
    fun `fileName strips path separators from the title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode.mp3",
            title = "Q&A: AC/DC? \"Live\"",
        )

        assertEquals("Q&A AC DC Live.mp3", name)
    }

    @Test
    fun `fileName falls back to the URL without a title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode-42.mp3",
            title = null,
        )

        assertEquals("episode-42.mp3", name)
    }

    @Test
    fun `fileName falls back to the URL with a blank title`() {
        val name = AudioDownloader.fileName(
            url = "https://example.com/episode-42.mp3",
            title = " / ",
        )

        assertEquals("episode-42.mp3", name)
    }
}
