package com.capyreader.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable
import com.capyreader.app.ui.theme.colorschemes.BaseColorScheme
import com.capyreader.app.ui.theme.colorschemes.GreenAppleColorScheme
import com.capyreader.app.ui.theme.colorschemes.LavenderColorScheme
import com.capyreader.app.ui.theme.colorschemes.MidnightDuskColorScheme
import com.capyreader.app.ui.theme.colorschemes.MonetColorScheme
import com.capyreader.app.ui.theme.colorschemes.MonochromeColorScheme
import com.capyreader.app.ui.theme.colorschemes.NordColorScheme
import com.capyreader.app.ui.theme.colorschemes.StrawberryColorScheme
import com.capyreader.app.ui.theme.colorschemes.TachiyomiColorScheme
import com.capyreader.app.ui.theme.colorschemes.TakoColorScheme
import com.capyreader.app.ui.theme.colorschemes.TealTurqoiseColorScheme
import com.capyreader.app.ui.theme.colorschemes.TidalWaveColorScheme
import com.capyreader.app.ui.theme.colorschemes.YinYangColorScheme
import com.capyreader.app.ui.theme.colorschemes.YotsubaColorScheme
import com.jocmp.capy.logging.CapyLog

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
        StatusBarColorListener(themeMode)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Composable
@ReadOnlyComposable
private fun getThemeColorScheme(
    appTheme: AppTheme,
    isDark: Boolean,
    pureBlack: Boolean,
): ColorScheme {
    val colorScheme = if (appTheme == AppTheme.MONET) {
        MonetColorScheme(LocalContext.current)
    } else {
        colorSchemes.getOrDefault(appTheme, TachiyomiColorScheme)
    }

    return colorScheme.getColorScheme(
        isDark = isDark,
        pureBlack,
    )
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.DEFAULT to TachiyomiColorScheme,
    AppTheme.GREEN_APPLE to GreenAppleColorScheme,
    AppTheme.LAVENDER to LavenderColorScheme,
    AppTheme.MIDNIGHT_DUSK to MidnightDuskColorScheme,
    AppTheme.MONOCHROME to MonochromeColorScheme,
    AppTheme.NORD to NordColorScheme,
    AppTheme.STRAWBERRY_DAIQUIRI to StrawberryColorScheme,
    AppTheme.TAKO to TakoColorScheme,
    AppTheme.TEAL_TURQUOISE to TealTurqoiseColorScheme,
    AppTheme.TIDAL_WAVE to TidalWaveColorScheme,
    AppTheme.YIN_YANG to YinYangColorScheme,
    AppTheme.YOTSUBA to YotsubaColorScheme,
)

@Composable
fun ThemeMode.showAppearanceLightStatusBars(): Boolean {
    return !(this == ThemeMode.DARK ||
            this == ThemeMode.SYSTEM && isSystemInDarkTheme())
}

@Composable
fun StatusBarColorListener(themeMode: ThemeMode) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    val isAppearanceLightStatusBars = themeMode.showAppearanceLightStatusBars()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            if (!isEdgeToEdgeAvailable()) {
                window.statusBarColor = colorScheme.surfaceContainer.toArgb()
            }

            CapyLog.info("bars", mapOf("light" to isAppearanceLightStatusBars, "mode" to themeMode))
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                isAppearanceLightStatusBars
        }
    }
}
