package com.jocmp.basil.common

internal fun String.prepending(tabCount: Int): String {
    if (tabCount < 1) {
        return this
    }

    return "${repeatTab(tabCount)}$this"
}

internal fun repeatTab(tabCount: Int): String {
    return " ".repeat(2 * tabCount)
}
