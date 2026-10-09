package com.capyreader.app.preferences

import com.capyreader.app.R

enum class BadgeStyle {
    SIMPLE,
    EXACT;

    val translationKey: Int
        get() = when (this) {
            SIMPLE -> R.string.badge_style_simple
            EXACT -> R.string.badge_style_exact
        }

    companion object {
        val default
            get() = EXACT
    }
}
