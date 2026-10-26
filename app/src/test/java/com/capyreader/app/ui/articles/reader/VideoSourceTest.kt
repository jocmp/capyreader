package com.capyreader.app.ui.articles.reader

import com.jocmp.mallet.LinearVideoSource
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VideoSourceTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `isPlayable allows progressive video`() {
        assertTrue(source("https://example.com/clip.mp4").isPlayable)
    }

    @Test
    fun `isPlayable allows HLS`() {
        assertTrue(source("https://example.com/stream/master.m3u8").isPlayable)
    }

    @Test
    fun `isPlayable allows YouTube embeds`() {
        val source = source(
            uri = "https://www.youtube.com/embed/abc123",
            link = "https://www.youtube.com/watch?v=abc123",
        )

        assertTrue(source.isPlayable)
    }

    @Test
    fun `isPlayable rejects DASH`() {
        assertFalse(source("https://example.com/stream/manifest.mpd").isPlayable)
    }

    @Test
    fun `isPlayable rejects SmoothStreaming`() {
        assertFalse(source("https://example.com/video.ism/Manifest").isPlayable)
    }

    @Test
    fun `isPlayable rejects RTSP`() {
        assertFalse(source("rtsp://example.com/live").isPlayable)
    }

    private fun source(uri: String, link: String = uri) = LinearVideoSource(
        uri = uri,
        link = link,
        imageThumbnail = null,
        widthPx = null,
        heightPx = null,
        mimeType = null,
    )
}
