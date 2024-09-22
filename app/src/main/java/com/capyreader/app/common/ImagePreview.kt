package com.capyreader.app.common

import com.capyreader.app.R
import com.capyreader.app.ui.settings.Translated

enum class ImagePreview : Translated {
    NONE,
    SMALL,
    LARGE;

    override val translationKey: Int
        get() = when (this) {
            NONE -> R.string.image_preview_menu_option_none
            SMALL -> R.string.image_preview_menu_option_small
            LARGE -> R.string.image_preview_menu_option_large
        }

    companion object {
        val default = SMALL

        val sorted: List<ImagePreview>
            get() = listOf(
                NONE,
                SMALL,
                LARGE,
            )
    }
}
