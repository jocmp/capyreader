package com.capyreader.app.ui.articles.detail

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

fun Modifier.inputKeyHandler(
    scrollState: ScrollState?,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    onToggleStar: () -> Unit,
    scrollUpKeyCode: Int,
    scrollDownKeyCode: Int,
    previousArticleKeyCode: Int,
    nextArticleKeyCode: Int,
    toggleStarKeyCode: Int,
): Modifier = composed {
    val scope = rememberCoroutineScope()

    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        val keyCode = keyEvent.key.nativeKeyCode
        when (keyCode) {
            scrollUpKeyCode -> {
                scrollState?.let { scope.launch { it.scrollBy(-SCROLL_AMOUNT) } }
                true
            }
            scrollDownKeyCode -> {
                scrollState?.let { scope.launch { it.scrollBy(SCROLL_AMOUNT) } }
                true
            }
            previousArticleKeyCode -> {
                onSelectPrevious()
                true
            }
            nextArticleKeyCode -> {
                onSelectNext()
                true
            }
            toggleStarKeyCode -> {
                onToggleStar()
                true
            }
            else -> false
        }
    }
}
