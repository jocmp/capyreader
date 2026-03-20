package com.capyreader.app.ui.articles.detail

import android.view.KeyEvent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.launch

private const val SCROLL_AMOUNT = 300f

fun Modifier.gameControllerHandler(
    scrollState: ScrollState?,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    onToggleStar: () -> Unit,
): Modifier = composed {
    val scope = rememberCoroutineScope()

    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        when (keyEvent.key.nativeKeyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                scrollState?.let { scope.launch { it.scrollBy(-SCROLL_AMOUNT) } }
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                scrollState?.let { scope.launch { it.scrollBy(SCROLL_AMOUNT) } }
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                onSelectPrevious()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                onSelectNext()
                true
            }
            KeyEvent.KEYCODE_BUTTON_A -> {
                onToggleStar()
                true
            }
            else -> false
        }
    }
}
