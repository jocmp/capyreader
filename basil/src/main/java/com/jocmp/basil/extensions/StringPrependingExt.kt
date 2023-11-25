package com.jocmp.basil.extensions

fun String.prepending(tabCount: Int): String {
    if (tabCount < 1) {
        return this
    }

    return "${repeatTab(tabCount)}$this"
}

fun repeatTab(tabCount: Int): String {
    return " ".repeat(2 * tabCount)
}
