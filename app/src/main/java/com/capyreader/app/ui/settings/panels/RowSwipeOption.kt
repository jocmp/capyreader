package com.capyreader.app.ui.settings.panels

import com.capyreader.app.R

enum class RowSwipeOption {
    DISABLED,
    TOGGLE_READ,
    TOGGLE_STARRED,
    OPEN_EXTERNALLY;

    val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_list_row_swipe_disabled
            TOGGLE_READ -> R.string.article_list_row_swipe_toggle_read
            TOGGLE_STARRED -> R.string.article_list_row_swipe_toggle_starred
            OPEN_EXTERNALLY -> R.string.article_list_row_swipe_open_externally
        }

    companion object {
        val default = TOGGLE_READ

        val sorted: List<RowSwipeOption>
            get() = listOf(
                DISABLED,
                TOGGLE_READ,
                TOGGLE_STARRED,
                OPEN_EXTERNALLY,
            )
    }
}
