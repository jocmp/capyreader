package com.capyreader.app.ui.articles

import com.capyreader.app.R

enum class MarkReadPosition {
    TOOLBAR,
    FLOATING_ACTION_BUTTON;

    val translationKey: Int
        get() = when(this) {
           TOOLBAR -> R.string.mark_read_position_toolbar
            FLOATING_ACTION_BUTTON -> R.string.mark_read_position_floating_action_button
        }

    companion object {
        val default
            get() = TOOLBAR
    }
}