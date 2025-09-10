package com.capyreader.app.preferences

import com.capyreader.app.R

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    val translationKey: Int
        get() = when(this) {
            SYSTEM -> R.string.theme_mode_system
            LIGHT -> R.string.theme_mode_light
            DARK -> R.string.theme_mode_dark
        }

    companion object {
        val default = SYSTEM
    }
}

enum class AppTheme {
    DEFAULT,
    MONET,
    GREEN_APPLE,
    LAVENDER,
    MIDNIGHT_DUSK,
    NORD,
    STRAWBERRY_DAIQUIRI,
    TAKO,
    TEAL_TURQUOISE,
    TIDAL_WAVE,
    YIN_YANG,
    YOTSUBA,
    MONOCHROME;

    val translationKey: Int
        get() = when(this) {
            DEFAULT -> R.string.theme_default
            MONET -> R.string.theme_monet
            GREEN_APPLE -> R.string.theme_green_apple
            LAVENDER -> R.string.theme_lavender
            MIDNIGHT_DUSK -> R.string.theme_midnight_dusk
            NORD -> R.string.theme_nord
            STRAWBERRY_DAIQUIRI -> R.string.theme_strawberry_daiquiri
            TAKO -> R.string.theme_tako
            TEAL_TURQUOISE -> R.string.theme_teal_turquoise
            TIDAL_WAVE -> R.string.theme_tidal_wave
            YIN_YANG -> R.string.theme_yin_yang
            YOTSUBA -> R.string.theme_yotsuba
            MONOCHROME -> R.string.theme_monochrome
        }

    companion object {
        val default = DEFAULT
        
        val sorted: List<AppTheme>
            get() = listOf(
                DEFAULT,
                MONET,
                GREEN_APPLE,
                LAVENDER,
                MIDNIGHT_DUSK,
                NORD,
                STRAWBERRY_DAIQUIRI,
                TAKO,
                TEAL_TURQUOISE,
                TIDAL_WAVE,
                YIN_YANG,
                YOTSUBA,
                MONOCHROME
            )
    }
}

data class ThemePreference(
    val themeMode: ThemeMode = ThemeMode.default,
    val appTheme: AppTheme = AppTheme.default,
    val pureBlackDarkMode: Boolean = false
) {
    companion object {
        val default = ThemePreference()
    }
}
