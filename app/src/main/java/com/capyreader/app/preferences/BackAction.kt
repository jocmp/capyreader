package com.capyreader.app.preferences

import com.capyreader.app.R

enum class BackAction {
    SYSTEM_BACK,
    OPEN_DRAWER,
    NAVIGATE_TO_PARENT;

    val translationKey: Int
        get() = when (this) {
            SYSTEM_BACK -> R.string.settings_gestures_list_back_navigation_system_back
            OPEN_DRAWER -> R.string.settings_gestures_list_back_navigation_open_drawer
            NAVIGATE_TO_PARENT -> R.string.settings_gestures_list_back_navigation_navigate_to_parent
        }

    companion object {
        val default = SYSTEM_BACK
    }
}
