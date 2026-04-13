package com.capyreader.app.keyboard

import android.view.KeyEvent
import androidx.annotation.StringRes
import com.capyreader.app.R
import kotlinx.serialization.Serializable

@Serializable
enum class ShortcutAction(
    @StringRes val labelRes: Int,
    val defaultKeys: List<ShortcutKey>,
) {
    NEXT_ARTICLE(
        labelRes = R.string.shortcut_next_article,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_J),
        ),
    ),
    PREVIOUS_ARTICLE(
        labelRes = R.string.shortcut_previous_article,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_K),
        ),
    ),
    TOGGLE_STAR(
        labelRes = R.string.shortcut_toggle_star,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_S),
        ),
    ),
    TOGGLE_READ(
        labelRes = R.string.shortcut_toggle_read,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_M),
        ),
    ),
    TOGGLE_FULL_CONTENT(
        labelRes = R.string.shortcut_toggle_full_content,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_C),
        ),
    ),
    OPEN_IN_BROWSER(
        labelRes = R.string.shortcut_open_in_browser,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_V),
        ),
    ),
    REFRESH(
        labelRes = R.string.shortcut_refresh,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_R),
        ),
    ),
    GO_BACK(
        labelRes = R.string.shortcut_go_back,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_ESCAPE),
        ),
    ),
    MARK_ALL_READ(
        labelRes = R.string.shortcut_mark_all_read,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_A, meta = KeyEvent.META_SHIFT_ON),
        ),
    ),
    FOCUS_SEARCH(
        labelRes = R.string.shortcut_focus_search,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_SLASH),
        ),
    ),
    TOGGLE_FULLSCREEN(
        labelRes = R.string.shortcut_toggle_fullscreen,
        defaultKeys = listOf(
            ShortcutKey(KeyEvent.KEYCODE_F, meta = KeyEvent.META_SHIFT_ON),
        ),
    );

    fun isCharacterBound(): Boolean {
        return defaultKeys.any { key ->
            key.meta == 0 && key.keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z
        }
    }
}
