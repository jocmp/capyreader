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
    SUNSET,
    NEWSPRINT,
    MONOCHROME,
    DEFAULT;

    val translationKey: Int
        get() = when (this) {
            MONET -> R.string.theme_dynamic
            DEFAULT -> R.string.theme_default
            SUNSET -> R.string.theme_sunset
            NEWSPRINT -> R.string.theme_newsprint
            MONOCHROME -> R.string.theme_monochrome
        }

    val supportsFeedAccentColor: Boolean
        get() = this == MONOCHROME || this == MONET

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
