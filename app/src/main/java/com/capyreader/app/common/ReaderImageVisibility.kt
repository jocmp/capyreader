package com.capyreader.app.common

import com.capyreader.app.R

enum class ReaderImageVisibility {
    ALWAYS_SHOW,
    ALWAYS_HIDE,
    SHOW_ON_WIFI;

    val translationKey: Int
        get() = when (this) {
            ALWAYS_SHOW -> R.string.reader_image_visibility_always_show
            ALWAYS_HIDE -> R.string.reader_image_visibility_always_hide
            SHOW_ON_WIFI ->R.string.reader_image_visibility_show_on_wifi
        }
}
