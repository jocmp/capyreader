package com.capyreader.app.ui.articles.detail

import android.view.KeyEvent
import androidx.compose.runtime.staticCompositionLocalOf
import com.capyreader.app.ui.articles.reader.PageDirection

val LocalPageTurnKeys = staticCompositionLocalOf { PageTurnKeys() }

class PageTurnKeys {
    private val handlers = mutableListOf<(PageDirection) -> Unit>()

    fun register(handler: (PageDirection) -> Unit): () -> Unit {
        handlers.add(handler)

        return { handlers.remove(handler) }
    }

    fun interceptKey(event: KeyEvent): Boolean {
        return dispatch(event, direction = interceptedDirection(event.keyCode))
    }

    fun unhandledKey(event: KeyEvent): Boolean {
        return dispatch(event, direction = unhandledDirection(event.keyCode))
    }

    private fun dispatch(event: KeyEvent, direction: PageDirection?): Boolean {
        val handler = handlers.lastOrNull()

        if (direction == null || handler == null) {
            return false
        }

        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            handler(direction)
        }

        return true
    }

    private fun interceptedDirection(keyCode: Int): PageDirection? {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_PAGE_DOWN -> PageDirection.FORWARD

            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_PAGE_UP -> PageDirection.BACK

            else -> null
        }
    }

    private fun unhandledDirection(keyCode: Int): PageDirection? {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT -> PageDirection.FORWARD

            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_LEFT -> PageDirection.BACK

            else -> null
        }
    }
}
