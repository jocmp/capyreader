package com.capyreader.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable
import com.capyreader.app.ui.theme.colorschemes.BaseColorScheme
import com.capyreader.app.ui.theme.colorschemes.MonochromeColorScheme
import com.capyreader.app.ui.theme.colorschemes.NewsprintColorScheme
import com.capyreader.app.ui.theme.colorschemes.SunsetColorScheme
import com.capyreader.app.ui.theme.colorschemes.TachiyomiColorScheme
import com.capyreader.app.ui.theme.colorschemes.applyPureBlack

val LocalAppTheme = staticCompositionLocalOf { AppTheme.DEFAULT }

@Composable
fun CapyTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    themeMode: ThemeMode = ThemeMode.default,
    pureBlack: Boolean = false,
    preview: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = getThemeColorScheme(
        appTheme = appTheme,
        isDark = isDark,
        pureBlack = pureBlack,
    )
    val view = LocalView.current

    if (!(preview || view.isInEditMode)) {
        StatusBarColorListener(colorScheme, themeMode, pureBlack)
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

@Composable
@ReadOnlyComposable
private fun getThemeColorScheme(
    appTheme: AppTheme,
    isDark: Boolean,
    pureBlack: Boolean,
): ColorScheme {
    val theme = appTheme.normalized()

    return if (theme == AppTheme.MONET && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) {
            dynamicDarkColorScheme(LocalContext.current)
                .applyPureBlack(pureBlack)
        } else {
            dynamicLightColorScheme(LocalContext.current)
        }
    } else {
        colorSchemes
            .getOrDefault(theme, TachiyomiColorScheme)
            .getColorScheme(
                isDark = isDark,
                pureBlack,
            )
    }
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.DEFAULT to TachiyomiColorScheme,
    AppTheme.SUNSET to SunsetColorScheme,
    AppTheme.MONOCHROME to MonochromeColorScheme,
    AppTheme.NEWSPRINT to NewsprintColorScheme,
)

@Composable
fun ThemeMode.showAppearanceLightStatusBars(): Boolean {
    return !(this == ThemeMode.DARK ||
            this == ThemeMode.SYSTEM && isSystemInDarkTheme())
}

@Composable
fun StatusBarColorListener(colorScheme: ColorScheme, themeMode: ThemeMode, pureBlack: Boolean) {
    val view = LocalView.current

    val isAppearanceLightStatusBars = themeMode.showAppearanceLightStatusBars()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            if (!isEdgeToEdgeAvailable()) {
                window.statusBarColor =
                    findStatusBarColor(colorScheme, pureBlack, isAppearanceLightStatusBars)
                        .toArgb()
            }

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                isAppearanceLightStatusBars
        }
    }
}

fun findStatusBarColor(
    colorScheme: ColorScheme,
    pureBlack: Boolean,
    isLightStatusBar: Boolean
): Color {
    return if (isLightStatusBar || !pureBlack) {
        colorScheme.surface
    } else {
        Color.Black
    }
}
