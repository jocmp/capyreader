package com.capyreader.app.ui.articles.detail

import android.view.KeyEvent
import com.capyreader.app.ui.articles.reader.PageDirection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PageTurnKeysTest {
    private val keys = PageTurnKeys()
    private val turns = mutableListOf<PageDirection>()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `volume keys turn pages while the reader is registered`() {
        keys.register { turns.add(it) }

        assertTrue(keys.interceptKey(down(KeyEvent.KEYCODE_VOLUME_DOWN)))
        assertTrue(keys.interceptKey(up(KeyEvent.KEYCODE_VOLUME_DOWN)))
        assertTrue(keys.interceptKey(down(KeyEvent.KEYCODE_VOLUME_UP)))

        assertEquals(listOf(PageDirection.FORWARD, PageDirection.BACK), turns)
    }

    @Test
    fun `volume keys pass through without a reader`() {
        val unregister = keys.register { turns.add(it) }
        unregister()

        assertFalse(keys.interceptKey(down(KeyEvent.KEYCODE_VOLUME_DOWN)))
        assertEquals(emptyList<PageDirection>(), turns)
    }

    @Test
    fun `held keys turn one page`() {
        keys.register { turns.add(it) }

        keys.interceptKey(down(KeyEvent.KEYCODE_PAGE_DOWN))
        keys.interceptKey(KeyEvent.changeTimeRepeat(down(KeyEvent.KEYCODE_PAGE_DOWN), 0L, 1))

        assertEquals(listOf(PageDirection.FORWARD), turns)
    }

    @Test
    fun `arrow keys only turn pages when nothing else handled them`() {
        keys.register { turns.add(it) }

        assertFalse(keys.interceptKey(down(KeyEvent.KEYCODE_DPAD_RIGHT)))
        assertTrue(keys.unhandledKey(down(KeyEvent.KEYCODE_DPAD_RIGHT)))

        assertEquals(listOf(PageDirection.FORWARD), turns)
    }

    private fun down(keyCode: Int) = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)

    private fun up(keyCode: Int) = KeyEvent(KeyEvent.ACTION_UP, keyCode)
}
