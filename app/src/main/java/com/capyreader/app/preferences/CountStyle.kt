package com.capyreader.app.preferences

import com.capyreader.app.R

enum class CountStyle {
    SIMPLE,
    EXACT;

    val translationKey: Int
        get() = when (this) {
            SIMPLE -> R.string.count_style_simple
            EXACT -> R.string.count_style_exact
        }

    companion object {
        val default
            get() = EXACT
    }
}
