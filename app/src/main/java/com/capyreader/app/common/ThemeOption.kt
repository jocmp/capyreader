package com.capyreader.app.common

import com.capyreader.app.R

enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM_DEFAULT;

    val translationKey: Int
        get() = when(this) {
            LIGHT -> R.string.theme_menu_option_light
            DARK -> R.string.theme_menu_option_dark
            SYSTEM_DEFAULT -> R.string.theme_menu_option_system_default
        }

    companion object {
        val default = SYSTEM_DEFAULT

        val sorted: List<ThemeOption>
            get() = listOf(
                SYSTEM_DEFAULT,
                LIGHT,
                DARK,
            )
    }
}
