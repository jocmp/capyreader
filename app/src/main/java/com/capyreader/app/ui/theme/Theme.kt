package com.capyreader.app.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.capyreader.app.common.ThemeOption

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CapyTheme(
    theme: ThemeOption = ThemeOption.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val showAppearanceLightStatusBars =
        !(theme == ThemeOption.DARK ||
                theme == ThemeOption.SYSTEM_DEFAULT && isSystemInDarkTheme())

    val colorScheme = when (theme) {
        ThemeOption.LIGHT -> lightScheme()
        ThemeOption.DARK -> darkScheme()
        ThemeOption.SYSTEM_DEFAULT -> systemDefaultScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = colorScheme.background.toArgb()
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
private fun lightScheme(): ColorScheme {
    val context = LocalContext.current

    return if (supportsDynamicColor()) {
        dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }
}

@Composable
private fun darkScheme(): ColorScheme {
    val context = LocalContext.current

    return if (supportsDynamicColor()) {
        dynamicDarkColorScheme(context)
    } else {
        DarkColorScheme
    }
}

@Composable
fun systemDefaultScheme(): ColorScheme {
    val context = LocalContext.current

    return if (isSystemInDarkTheme()) {
        darkScheme()
    } else {
        lightScheme()
    }
}


fun supportsDynamicColor() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
