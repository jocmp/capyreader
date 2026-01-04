package com.capyreader.app.ui.articles.detail

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent

fun Modifier.articleKeyboardHandler(
    isDetailPaneFocused: Boolean,
    onShortcut: (ArticleShortcut) -> Unit,
): Modifier = onKeyEvent { event ->
    if (event.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) {
        return@onKeyEvent false
    }

    val shortcut = mapKeyToShortcut(
        keyCode = event.nativeKeyEvent.keyCode,
        isDetailPaneFocused = isDetailPaneFocused,
    ) ?: return@onKeyEvent false

    onShortcut(shortcut)
    true
}

private fun mapKeyToShortcut(keyCode: Int, isDetailPaneFocused: Boolean): ArticleShortcut? {
    return when (keyCode) {
        KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_DPAD_DOWN -> {
            if (isDetailPaneFocused) ArticleShortcut.ScrollDown else ArticleShortcut.NextArticle
        }
        KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_DPAD_UP -> {
            if (isDetailPaneFocused) ArticleShortcut.ScrollUp else ArticleShortcut.PreviousArticle
        }
        KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_DPAD_LEFT -> ArticleShortcut.FocusList
        KeyEvent.KEYCODE_L, KeyEvent.KEYCODE_DPAD_RIGHT -> ArticleShortcut.FocusDetail
        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> ArticleShortcut.OpenInBrowser
        KeyEvent.KEYCODE_S -> ArticleShortcut.ToggleStar
        KeyEvent.KEYCODE_M -> ArticleShortcut.ToggleRead
        KeyEvent.KEYCODE_C -> ArticleShortcut.ToggleFullContent
        KeyEvent.KEYCODE_ESCAPE -> ArticleShortcut.GoBack
        else -> null
    }
}
