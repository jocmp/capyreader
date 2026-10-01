package com.capyreader.app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable
import com.capyreader.app.ui.theme.LocalAppTheme

@Composable
fun PureBlackNavigationBar(appPreferences: AppPreferences) {
    val view = LocalView.current
    val pureBlack by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()
    val isPureBlackDark = pureBlack && LocalAppTheme.current.isDark

    if (!isEdgeToEdgeAvailable() || view.isInEditMode) {
        return
    }

    SideEffect {
        val window = (view.context as Activity).window

        window.isNavigationBarContrastEnforced = !isPureBlackDark
    }

    if (!isPureBlackDark) {
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.tappableElement)
                .background(Color.Black)
        )
    }
}
