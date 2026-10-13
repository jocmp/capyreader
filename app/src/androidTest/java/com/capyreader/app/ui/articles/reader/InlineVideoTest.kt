package com.capyreader.app.ui.articles.reader

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.capyreader.app.MainActivity
import com.capyreader.app.R
import com.capyreader.app.testing.OfflineAccountRule
import com.capyreader.app.testing.OfflineArticle
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime
import androidx.media3.ui.compose.material3.R as Media3R

@RunWith(AndroidJUnit4::class)
class InlineVideoTest {
    @get:Rule
    val offlineAccount = OfflineAccountRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val device = UiDevice.getInstance(instrumentation)

    private val server = MockWebServer()

    private val video by lazy {
        instrumentation.context.assets.open(VIDEO_ASSET).use { it.readBytes() }
    }

    @Before
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse.Builder()
                    .addHeader("Content-Type", "video/mp4")
                    .body(Buffer().write(video))
                    .build()
            }
        }
        server.start()

        offlineAccount.delegate.seed(
            listOf(
                OfflineArticle(
                    id = ARTICLE_ID,
                    title = ARTICLE_TITLE,
                    publishedAt = ZonedDateTime.now(),
                    contentHTML = """
                        <p>$ARTICLE_TITLE</p>
                        <video src="${server.url("/$VIDEO_ASSET")}"></video>
                    """.trimIndent(),
                )
            )
        )
    }

    @After
    fun tearDown() {
        device.setOrientationNatural()
        device.unfreezeRotation()
        server.close()
    }

    @Test
    fun framesPortraitVideoBeforePlaying() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openArticle()

            val icon = waitForDescription(string(R.string.reader_play_video))
            val bounds = clickableAncestor(icon).visibleBounds

            assertTrue(
                "Expected a portrait frame, got ${bounds.width()}x${bounds.height()}",
                bounds.height() > bounds.width(),
            )
        }
    }

    @Test
    fun playRestartsFinishedVideo() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openArticle()
            playUntilFinished()

            waitForDescription(playLabel).click()

            assertTrue(
                "Expected playback to resume and hide the controls",
                device.wait(Until.gone(By.desc(playLabel)), TIMEOUT_MILLIS) == true,
            )
        }
    }

    @Test
    fun fullscreenKeepsPlayerAcrossRotation() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openArticle()
            playUntilFinished()

            waitForDescription(string(R.string.reader_video_enter_fullscreen)).click()
            waitForDescription(string(R.string.reader_video_exit_fullscreen))

            device.setOrientationLeft()
            SystemClock.sleep(SETTLE_MILLIS)

            waitForDescription(string(R.string.reader_video_exit_fullscreen))
            waitForDescription(playLabel)

            device.setOrientationNatural()
            SystemClock.sleep(SETTLE_MILLIS)
            device.pressBack()

            waitForDescription(string(R.string.reader_video_enter_fullscreen))
        }
    }

    private val playLabel
        get() = string(Media3R.string.playpause_button_play)

    private fun openArticle() {
        val row = device.wait(Until.findObject(By.text(ARTICLE_TITLE)), TIMEOUT_MILLIS)
        assertNotNull("Expected the seeded article in the list", row)
        row.click()
    }

    private fun playUntilFinished() {
        waitForDescription(string(R.string.reader_play_video)).click()
        SystemClock.sleep(VIDEO_DURATION_MILLIS + SETTLE_MILLIS)
        waitForDescription(playLabel)
    }

    private fun clickableAncestor(element: UiObject2): UiObject2 {
        return generateSequence(element) { it.parent }.first { it.isClickable }
    }

    private fun waitForDescription(description: String): UiObject2 {
        val found = device.wait(Until.findObject(By.desc(description)), TIMEOUT_MILLIS)
        assertNotNull("Expected an element described as \"$description\"", found)
        return found
    }

    private fun string(id: Int) = instrumentation.targetContext.getString(id)

    companion object {
        private const val ARTICLE_ID = "video-article"
        private const val ARTICLE_TITLE = "Portrait video article"
        private const val VIDEO_ASSET = "portrait.mp4"
        private const val VIDEO_DURATION_MILLIS = 2_000L
        private const val TIMEOUT_MILLIS = 5_000L
        private const val SETTLE_MILLIS = 1_000L
    }
}
