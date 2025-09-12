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
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )

    override val lightScheme: ColorScheme = lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )
}

val primaryLight = Color(0xFF5F5E59)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFF5F2EB)
val onPrimaryContainerLight = Color(0xFF6F6E69)
val secondaryLight = Color(0xFF5F5E5C)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE5E2DE)
val onSecondaryContainerLight = Color(0xFF666461)
val tertiaryLight = Color(0xFF5D5F5C)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFF2F3EF)
val onTertiaryContainerLight = Color(0xFF6D6F6C)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFDF8F7)
val onBackgroundLight = Color(0xFF1C1B1B)
val surfaceLight = Color(0xFFFDF8F7)
val onSurfaceLight = Color(0xFF1C1B1B)
val surfaceVariantLight = Color(0xFFE5E2D9)
val onSurfaceVariantLight = Color(0xFF474740)
val outlineLight = Color(0xFF787770)
val outlineVariantLight = Color(0xFFC8C7BE)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF313030)
val inverseOnSurfaceLight = Color(0xFFF4F0EE)
val inversePrimaryLight = Color(0xFFC9C6C0)
val surfaceDimLight = Color(0xFFDDD9D8)
val surfaceBrightLight = Color(0xFFFDF8F7)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF7F3F1)
val surfaceContainerLight = Color(0xFFF1EDEC)
val surfaceContainerHighLight = Color(0xFFEBE7E6)
val surfaceContainerHighestLight = Color(0xFFE5E2E0)


val primaryDark = Color(0xFFFFFFFF)
val onPrimaryDark = Color(0xFF31312C)
val primaryContainerDark = Color(0xFFE5E2DB)
val onPrimaryContainerDark = Color(0xFF65645F)
val secondaryDark = Color(0xFFC9C6C3)
val onSecondaryDark = Color(0xFF31302E)
val secondaryContainerDark = Color(0xFF484744)
val onSecondaryContainerDark = Color(0xFFB7B5B1)
val tertiaryDark = Color(0xFFFFFFFF)
val onTertiaryDark = Color(0xFF2E312F)
val tertiaryContainerDark = Color(0xFFE2E3DF)
val onTertiaryContainerDark = Color(0xFF636562)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF141313)
val onBackgroundDark = Color(0xFFE5E2E0)
val surfaceDark = Color(0xFF141313)
val onSurfaceDark = Color(0xFFE5E2E0)
val surfaceVariantDark = Color(0xFF474740)
val onSurfaceVariantDark = Color(0xFFC8C7BE)
val outlineDark = Color(0xFF929189)
val outlineVariantDark = Color(0xFF474740)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE5E2E0)
val inverseOnSurfaceDark = Color(0xFF313030)
val inversePrimaryDark = Color(0xFF5F5E59)
val surfaceDimDark = Color(0xFF141313)
val surfaceBrightDark = Color(0xFF3A3938)
val surfaceContainerLowestDark = Color(0xFF0E0E0E)
val surfaceContainerLowDark = Color(0xFF1C1B1B)
val surfaceContainerDark = Color(0xFF201F1F)
val surfaceContainerHighDark = Color(0xFF2B2A29)
val surfaceContainerHighestDark = Color(0xFF353434)
