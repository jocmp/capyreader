package com.capyreader.app.ui

import androidx.navigation3.runtime.NavKey

/**
 * Pops the top entry, but never the root. NavDisplay throws on an empty back stack.
 */
internal fun MutableList<NavKey>.pop() {
    if (size > 1) {
        removeAt(lastIndex)
    }
}

/**
 * Pops [key] only while it is the top entry. An exiting screen stays composed (and clickable)
 * during its pop transition, so a repeated tap on its close button must not pop the entry below.
 */
internal fun MutableList<NavKey>.pop(key: NavKey) {
    popIf { it == key }
}

/** Like [pop] with a key, for entries whose key is swapped in place (reader next/previous). */
internal fun MutableList<NavKey>.popIf(isTop: (NavKey) -> Boolean) {
    if (lastOrNull()?.let(isTop) == true) {
        pop()
    }
}
