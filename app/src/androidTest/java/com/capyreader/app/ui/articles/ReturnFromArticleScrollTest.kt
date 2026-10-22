package com.capyreader.app.ui.articles

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.ZonedDateTime

@RunWith(Parameterized::class)
class ReturnFromArticleScrollTest(markReadOnScroll: Boolean) {
    @get:Rule
    val offlineAccount = OfflineAccountRule(
        filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD),
        markReadOnScroll = markReadOnScroll,
    )

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun closingArticleKeepsListPosition() {
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
            SystemClock.sleep(SETTLE_MILLIS)

            val rows = device.findObjects(By.textStartsWith("Seed article "))
            rows[rows.size / 2].click()

            assertTrue(
                "Expected the article to open",
                device.wait(Until.hasObject(By.desc(string(R.string.article_style_options))), TIMEOUT_MILLIS) == true
            )

            device.pressBack()

            assertTrue(
                "Expected the article to close",
                device.wait(Until.gone(By.desc(string(R.string.article_style_options))), TIMEOUT_MILLIS) == true
            )
            assertTrue(waitForText("Seed article "))
            SystemClock.sleep(SETTLE_MILLIS)

            assertFalse(
                "Expected the list to keep its scroll position after closing the article",
                device.hasObject(By.text("Seed article 1"))
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

    private fun waitForText(prefix: String): Boolean {
        return device.wait(Until.hasObject(By.textStartsWith(prefix)), TIMEOUT_MILLIS) == true
    }

    private fun string(id: Int) = instrumentation.targetContext.getString(id)

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val SETTLE_MILLIS = 1_500L
        private const val SWIPE_STEPS = 50

        @JvmStatic
        @Parameterized.Parameters(name = "markReadOnScroll={0}")
        fun parameters() = listOf(false, true)
    }
}
