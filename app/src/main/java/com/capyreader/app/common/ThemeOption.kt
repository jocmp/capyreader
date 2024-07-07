package com.capyreader.app.common

enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM_DEFAULT;

    companion object {
        val default = SYSTEM_DEFAULT
    }
}
