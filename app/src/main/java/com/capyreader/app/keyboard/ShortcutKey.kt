package com.capyreader.app.keyboard

import android.view.KeyEvent
import kotlinx.serialization.Serializable

@Serializable
data class ShortcutKey(
    val keyCode: Int,
    val meta: Int = 0,
) {
    fun label(): String {
        val parts = mutableListOf<String>()

        if (meta and KeyEvent.META_CTRL_ON != 0) parts.add("Ctrl")
        if (meta and KeyEvent.META_ALT_ON != 0) parts.add("Alt")
        if (meta and KeyEvent.META_SHIFT_ON != 0) parts.add("Shift")
        if (meta and KeyEvent.META_META_ON != 0) parts.add("Meta")

        parts.add(keyCodeLabel(keyCode))

        return parts.joinToString("+")
    }

    companion object {
        private fun keyCodeLabel(keyCode: Int): String {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> "Up"
                KeyEvent.KEYCODE_DPAD_DOWN -> "Down"
                KeyEvent.KEYCODE_DPAD_LEFT -> "Left"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "Right"
                KeyEvent.KEYCODE_ENTER -> "Enter"
                KeyEvent.KEYCODE_SPACE -> "Space"
                KeyEvent.KEYCODE_ESCAPE -> "Esc"
                KeyEvent.KEYCODE_DEL -> "Backspace"
                KeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
                KeyEvent.KEYCODE_TAB -> "Tab"
                else -> KeyEvent.keyCodeToString(keyCode)
                    .removePrefix("KEYCODE_")
                    .lowercase()
                    .replaceFirstChar { it.uppercase() }
            }
        }

        fun isModifierOnly(keyCode: Int): Boolean {
            return keyCode in listOf(
                KeyEvent.KEYCODE_SHIFT_LEFT,
                KeyEvent.KEYCODE_SHIFT_RIGHT,
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_CTRL_RIGHT,
                KeyEvent.KEYCODE_ALT_LEFT,
                KeyEvent.KEYCODE_ALT_RIGHT,
                KeyEvent.KEYCODE_META_LEFT,
                KeyEvent.KEYCODE_META_RIGHT,
            )
        }

        fun metaState(event: KeyEvent): Int {
            var meta = 0
            if (event.isCtrlPressed) meta = meta or KeyEvent.META_CTRL_ON
            if (event.isAltPressed) meta = meta or KeyEvent.META_ALT_ON
            if (event.isShiftPressed) meta = meta or KeyEvent.META_SHIFT_ON
            if (event.isMetaPressed) meta = meta or KeyEvent.META_META_ON
            return meta
        }
    }
}
