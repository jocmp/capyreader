package com.capyreader.app.preferences

import com.capyreader.app.R

enum class BadgeStyle {
    EXACT,
    SIMPLE,
    HIDDEN;

    val translationKey: Int
        get() = when (this) {
            EXACT -> R.string.badge_style_count
            SIMPLE -> R.string.badge_style_dot
            HIDDEN -> R.string.badge_style_hide
        }

    companion object {
        val default
            get() = EXACT
    }
}
