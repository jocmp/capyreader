package com.capyreader.app.common

import com.capyreader.app.R

enum class ImagePreview {
    NONE,
    SMALL,
    MEDIUM,
    LARGE;

    val translationKey: Int
        get() = when (this) {
            NONE -> R.string.image_preview_menu_option_none
            SMALL -> R.string.image_preview_menu_option_small
            MEDIUM -> R.string.image_preview_menu_option_medium
            LARGE -> R.string.image_preview_menu_option_large
        }

    fun showInline(): Boolean {
        return this == SMALL || this == MEDIUM
    }

    companion object {
        val default = MEDIUM

        val sorted: List<ImagePreview>
            get() = listOf(
                NONE,
                SMALL,
                MEDIUM,
                LARGE,
            )
    }
}
