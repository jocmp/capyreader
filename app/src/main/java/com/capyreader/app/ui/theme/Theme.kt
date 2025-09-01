package com.capyreader.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable
import org.koin.compose.koinInject

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

@Composable
fun CapyTheme(
    theme: ThemeOption = ThemeOption.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val showAppearanceLightStatusBars = theme.showAppearanceLightStatusBars()

    val colorScheme = when (theme) {
        ThemeOption.LIGHT -> lightScheme()
        ThemeOption.DARK -> darkScheme()
        ThemeOption.SYSTEM_DEFAULT -> systemDefaultScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            if (!isEdgeToEdgeAvailable()) {
                window.statusBarColor = colorScheme.surfaceContainer.toArgb()
            }

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                showAppearanceLightStatusBars
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ThemeOption.showAppearanceLightStatusBars(): Boolean {
    return !(this == ThemeOption.DARK ||
            this == ThemeOption.SYSTEM_DEFAULT && isSystemInDarkTheme())
}


@Composable
private fun lightScheme(): ColorScheme {
    val context = LocalContext.current

    return if (supportsDynamicColor()) {
        dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }
}

@Composable
private fun darkScheme(appPreferences: AppPreferences = koinInject()): ColorScheme {
    val context = LocalContext.current
    val enableTrueBlack by appPreferences.enableHighContrastDarkTheme
        .changes()
        .collectAsState(appPreferences.enableHighContrastDarkTheme.get())

    return if (enableTrueBlack) {
        highContrastDarkColorScheme
    } else if (supportsDynamicColor()) {
        dynamicDarkColorScheme(context)
    } else {
        DarkColorScheme
    }
}

@Composable
fun systemDefaultScheme(): ColorScheme {
    return if (isSystemInDarkTheme()) {
        darkScheme()
    } else {
        lightScheme()
    }
}


fun supportsDynamicColor() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
