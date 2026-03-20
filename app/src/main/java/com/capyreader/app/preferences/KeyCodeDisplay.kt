package com.capyreader.app.preferences

import android.view.KeyEvent

fun keyCodeToDisplayName(keyCode: Int): String {
    if (keyCode < 0) return "Disabled"
    val name = KeyEvent.keyCodeToString(keyCode)
    return name.removePrefix("KEYCODE_")
        .replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
