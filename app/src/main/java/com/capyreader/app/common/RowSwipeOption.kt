package com.capyreader.app.common

import com.capyreader.app.R
import com.capyreader.app.ui.settings.Translated

enum class RowSwipeOption : Translated {
    DISABLED,
    TOGGLE_READ,
    TOGGLE_STARRED;

    override val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_list_row_swipe_disabled
            TOGGLE_READ -> R.string.article_list_row_swipe_toggle_read
            TOGGLE_STARRED -> R.string.article_list_row_swipe_toggle_starred
        }

    companion object {
        val default = TOGGLE_READ

        val sorted: List<RowSwipeOption>
            get() = listOf(
                DISABLED,
                TOGGLE_READ,
                TOGGLE_STARRED,
            )
    }
}
