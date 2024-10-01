package com.capyreader.app.common

import com.capyreader.app.R

enum class BackAction {
    SYSTEM_BACK,
    OPEN_DRAWER;

    val translationKey: Int
        get() = when (this) {
            SYSTEM_BACK -> R.string.settings_gestures_list_back_navigation_system_back
            OPEN_DRAWER -> R.string.settings_gestures_list_back_navigation_open_drawer
        }

    companion object {
        val default = SYSTEM_BACK
    }
}
