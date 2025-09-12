package com.capyreader.app.preferences

import android.os.Build
import com.capyreader.app.R

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    val translationKey: Int
        get() = when (this) {
            SYSTEM -> R.string.theme_mode_system
            LIGHT -> R.string.theme_mode_light
            DARK -> R.string.theme_mode_dark
        }

    companion object {
        val default = SYSTEM
    }
}

enum class AppTheme {
    MONET,
    DEFAULT,
    SUNSET,
    YIN_YANG,
    MONOCHROME,
    GREEN_APPLE,
    LAVENDER,
    MIDNIGHT_DUSK,
    NORD,
    STRAWBERRY_DAIQUIRI,
    TAKO,
    TEAL_TURQUOISE,
    TIDAL_WAVE,
    YOTSUBA;

    val translationKey: Int
        get() = when (this) {
            MONET -> R.string.theme_dynamic
            DEFAULT -> R.string.theme_default
            SUNSET -> R.string.theme_sunset
            YIN_YANG -> R.string.theme_yin_yang
            MONOCHROME -> R.string.theme_monochrome
            GREEN_APPLE -> R.string.theme_green_apple
            LAVENDER -> R.string.theme_lavender
            MIDNIGHT_DUSK -> R.string.theme_midnight_dusk
            NORD -> R.string.theme_nord
            STRAWBERRY_DAIQUIRI -> R.string.theme_strawberry_daiquiri
            TAKO -> R.string.theme_tako
            TEAL_TURQUOISE -> R.string.theme_teal_turquoise
            TIDAL_WAVE -> R.string.theme_tidal_wave
            YOTSUBA -> R.string.theme_yotsuba
        }

    /** On the off chance someone has selected MONET on an older version, default them */
    fun normalized(): AppTheme {
        return if (this === MONET && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            DEFAULT
        } else {
            this
        }
    }

    companion object {
        val default = MONET.normalized()
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
