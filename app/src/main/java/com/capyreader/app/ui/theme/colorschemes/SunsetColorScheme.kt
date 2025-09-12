package com.capyreader.app.ui.theme.colorschemes

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Primary color: #f5f2eb;
 *
 * From [Feedbin](https://github.com/feedbin/feedbin/blob/801225f11d4cbd0b674c758b29e6f861de32bbf0/app/assets/stylesheets/application.scss#L30-L37)
 */
internal object SunsetColorScheme : BaseColorScheme() {
    override val darkScheme: ColorScheme = darkColorScheme(
        primary = Color(0xFFFFFFFF),
        onPrimary = Color(0xFF31312C),
        primaryContainer = Color(0xFFE5E2DB),
        onPrimaryContainer = Color(0xFF65645F),
        secondary = Color(0xFFC9C6C3),
        onSecondary = Color(0xFF31302E),
        secondaryContainer = Color(0xFF484744),
        onSecondaryContainer = Color(0xFFB7B5B1),
        tertiary = Color(0xFFFFFFFF),
        onTertiary = Color(0xFF2E312F),
        tertiaryContainer = Color(0xFFE2E3DF),
        onTertiaryContainer = Color(0xFF636562),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF141313),
        onBackground = Color(0xFFE5E2E0),
        surface = Color(0xFF141313),
        onSurface = Color(0xFFE5E2E0),
        surfaceVariant = Color(0xFF474740),
        onSurfaceVariant = Color(0xFFC8C7BE),
        outline = Color(0xFF929189),
        outlineVariant = Color(0xFF474740),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE5E2E0),
        inverseOnSurface = Color(0xFF313030),
        inversePrimary = Color(0xFF5F5E59),
        surfaceDim = Color(0xFF141313),
        surfaceBright = Color(0xFF3A3938),
        surfaceContainerLowest = Color(0xFF0E0E0E),
        surfaceContainerLow = Color(0xFF1C1B1B),
        surfaceContainer = Color(0xFF201F1F),
        surfaceContainerHigh = Color(0xFF2B2A29),
        surfaceContainerHighest = Color(0xFF353434),
    )

    override val lightScheme: ColorScheme = lightColorScheme(
        primary = Color(0xFF5F5E59),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF5F2EB),
        onPrimaryContainer = Color(0xFF6F6E69),
        secondary = Color(0xFF5F5E5C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF6e6d69), // -- theme-color-sunset-500
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF5D5F5C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2F3EF),
        onTertiaryContainer = Color(0xFF6D6F6C),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFFDF8F7),
        onBackground = Color(0xFF1C1B1B),
        surface = Color(0xFFFDF8F7),
        onSurface = Color(0xFF1C1B1B),
        surfaceVariant = Color(0xFFE5E2D9),
        onSurfaceVariant = Color(0xFF474740),
        outline = Color(0xFF787770),
        outlineVariant = Color(0xFFC8C7BE),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF313030),
        inverseOnSurface = Color(0xFFF4F0EE),
        inversePrimary = Color(0xFFC9C6C0),
        surfaceDim = Color(0xFFDDD9D8),
        surfaceBright = Color(0xFFFDF8F7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F3F1),
        surfaceContainer = Color(0xFFF1EDEC),
        surfaceContainerHigh = Color(0xFFEBE7E6),
        surfaceContainerHighest = Color(0xFFE5E2E0),
    )
}
