package com.capyreader.app.ui.articles

import android.os.SystemClock
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
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class MarkReadOnScrollTest {
    @get:Rule
    val offlineAccount = OfflineAccountRule(
        filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD),
        markReadOnScroll = true,
    )

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val device = UiDevice.getInstance(instrumentation)

    private val delegate
        get() = offlineAccount.delegate

    @Test
    fun refreshMidScrollKeepsNewArticlesUnread() {
        val now = ZonedDateTime.now()
        val seeded = (1..40).map {
            OfflineArticle(
                id = "seed-$it",
                title = "Seed article $it",
                publishedAt = now.minusHours(it.toLong()),
            )
        }
        val fresh = (1..10).map {
            OfflineArticle(
                id = "fresh-$it",
                title = "Fresh article $it",
                publishedAt = now.minusMinutes(it.toLong()),
            )
        }

        delegate.seed(seeded)

        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrue(waitForText("Seed article 1"))

            repeat(4) { swipeUp() }

            assertTrue(
                "Expected scrolling to mark seeded articles read",
                waitUntil { readIDs(seeded).isNotEmpty() }
            )

            val readBeforeRefresh = readIDs(seeded)
            val release = CompletableDeferred<Unit>()
            delegate.enqueueRefresh(fresh, release)

            openDrawer()
            device.findObject(By.desc(string(R.string.feed_nav_drawer_refresh_all))).click()
            closeDrawer()

            repeat(2) { swipeUp() }
            SystemClock.sleep(SETTLE_MILLIS)

            release.complete(Unit)

            assertTrue(waitForText("Fresh article 1"))
            SystemClock.sleep(SETTLE_MILLIS)

            assertEquals(emptyList<String>(), readIDs(fresh))
            assertTrue(readBeforeRefresh.all(delegate::isRead))

            swipeUp()

            assertTrue(
                "Expected the reset watermark to mark fresh articles read on the next scroll",
                waitUntil { readIDs(fresh).isNotEmpty() }
            )
        }
    }

    private fun readIDs(articles: List<OfflineArticle>): List<String> {
        return articles.map { it.id }.filter(delegate::isRead)
    }

    private fun openDrawer() {
        device.findObject(By.desc(string(R.string.feed_nav_drawer_open))).click()
        waitForDescription(string(R.string.feed_nav_drawer_refresh_all))
    }

    private fun closeDrawer() {
        device.pressBack()
        device.wait(Until.gone(By.desc(string(R.string.feed_nav_drawer_refresh_all))), TIMEOUT_MILLIS)
        SystemClock.sleep(SETTLE_MILLIS)
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

    private fun waitForDescription(description: String): Boolean {
        return device.wait(Until.hasObject(By.desc(description)), TIMEOUT_MILLIS) == true
    }

    private fun waitUntil(condition: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MILLIS

        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) {
                return true
            }
            SystemClock.sleep(POLL_MILLIS)
        }

        return condition()
    }

    private fun string(id: Int) = instrumentation.targetContext.getString(id)

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val POLL_MILLIS = 100L
        private const val SETTLE_MILLIS = 1_000L
        private const val SWIPE_STEPS = 50
    }
}
