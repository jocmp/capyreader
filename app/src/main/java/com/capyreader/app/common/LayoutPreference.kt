package com.capyreader.app.common

import com.capyreader.app.R

enum class LayoutPreference {
    RESPONSIVE,
    SINGLE;

    val translationKey: Int
        get() = when(this) {
            RESPONSIVE -> R.string.layout_preference_responsive
            SINGLE -> R.string.layout_preference_single
        }
}
