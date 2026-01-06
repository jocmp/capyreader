package com.capyreader.app.ui.articles.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.capyreader.app.common.AudioEnclosure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioPlayerControllerTest {
    private lateinit var controller: AudioPlayerController

    private val testAudio = AudioEnclosure(
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        title = "Test Audio",
        feedName = "Test Feed",
        durationSeconds = 60,
        artworkUrl = null,
    )

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        controller = AudioPlayerController(context)
    }

    @After
    fun teardown() {
        controller.dismiss()
        controller.release()
    }

    @Test
    fun initialState_hasNullCurrentAudio() = runBlocking {
        val currentAudio = controller.currentAudio.first()
        assertNull(currentAudio)
    }

    @Test
    fun initialState_isNotPlaying() = runBlocking {
        val isPlaying = controller.isPlaying.first()
        assertEquals(false, isPlaying)
    }

    @Test
    fun initialState_positionIsZero() = runBlocking {
        val position = controller.currentPosition.first()
        assertEquals(0L, position)
    }

    @Test
    fun play_setsCurrentAudio() = runBlocking {
        controller.play(testAudio)

        withTimeout(5000) {
            while (controller.currentAudio.value == null) {
                delay(100)
            }
        }

        val currentAudio = controller.currentAudio.first()
        assertNotNull(currentAudio)
        assertEquals(testAudio.url, currentAudio?.url)
        assertEquals(testAudio.title, currentAudio?.title)
    }

    @Test
    fun dismiss_clearsCurrentAudio() = runBlocking {
        controller.play(testAudio)

        withTimeout(5000) {
            while (controller.currentAudio.value == null) {
                delay(100)
            }
        }

        controller.dismiss()

        withTimeout(2000) {
            while (controller.currentAudio.value != null) {
                delay(100)
            }
        }

        val currentAudio = controller.currentAudio.first()
        assertNull(currentAudio)
    }

    @Test
    fun seekTo_updatesPosition() = runBlocking {
        controller.play(testAudio)

        withTimeout(5000) {
            while (controller.currentAudio.value == null) {
                delay(100)
            }
        }

        val seekPosition = 30_000L
        controller.seekTo(seekPosition)

        withTimeout(2000) {
            while (controller.currentPosition.value != seekPosition) {
                delay(100)
            }
        }

        val position = controller.currentPosition.first()
        assertEquals(seekPosition, position)
    }
}
