package com.capyreader.app.ui.articles

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.capyreader.app.MainActivity
import com.capyreader.app.R
import com.capyreader.app.testing.OfflineAccountRule
import com.capyreader.app.testing.OfflineArticle
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class StatusReselectScrollTest {
    @get:Rule
    val offlineAccount = OfflineAccountRule(
        filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD),
    )

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun reselectingStatusScrollsToTop() {
        val now = ZonedDateTime.now()
        val seeded = (1..40).map {
            OfflineArticle(
                id = "seed-$it",
                title = "Seed article $it",
                publishedAt = now.minusHours(it.toLong()),
            )
        }

        offlineAccount.delegate.seed(seeded)

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrue(waitForText("Seed article 1"))

            repeat(4) { swipeUp() }

            assertTrue(
                "Expected the first article to scroll out of view",
                device.wait(Until.gone(By.text("Seed article 1")), TIMEOUT_MILLIS) == true
            )

            device.findObject(By.desc(string(R.string.filter_unread))).click()

            assertTrue(
                "Expected reselecting the current status to scroll to the top",
                waitForText("Seed article 1")
            )
        }
    }

    private fun swipeUp() {
        val x = device.displayWidth / 2

        device.swipe(
            x,
            (device.displayHeight * 0.75).toInt(),
            x,
            (device.displayHeight * 0.35).toInt(),
            SWIPE_STEPS,
        )
    }

    private fun waitForText(text: String): Boolean {
        return device.wait(Until.hasObject(By.text(text)), TIMEOUT_MILLIS) == true
    }

    private fun string(id: Int) = instrumentation.targetContext.getString(id)

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val SWIPE_STEPS = 50
    }
}
