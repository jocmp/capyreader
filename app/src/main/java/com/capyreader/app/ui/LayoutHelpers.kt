package com.capyreader.app.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.window.core.layout.WindowWidthSizeClass.Companion.EXPANDED
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.common.asState
import org.koin.compose.koinInject

@Composable
fun rememberLayoutPreference(appPreferences: AppPreferences = koinInject()): LayoutPreference {
    val layout by appPreferences.layout.asState()

    return if (isExpanded()) {
        layout
    } else {
        LayoutPreference.RESPONSIVE
    }
}

@Composable
fun currentWindowSizeClass() = currentWindowAdaptiveInfo().windowSizeClass

@Composable
fun isAtMostMedium(): Boolean {
    return currentWindowSizeClass().windowWidthSizeClass != EXPANDED
}

@Composable
fun isExpanded(): Boolean {
    return currentWindowSizeClass().windowWidthSizeClass == EXPANDED
}

@Composable
fun isCompact(): Boolean {
    return currentWindowSizeClass().windowWidthSizeClass != EXPANDED
}
