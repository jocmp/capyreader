package com.capyreader.app.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND

@Composable
fun currentWindowSizeClass() = currentWindowAdaptiveInfo().windowSizeClass

@Composable
fun isExpanded(): Boolean {
    return currentWindowSizeClass().isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
}

@Composable
fun isLarge(): Boolean {
    return currentWindowSizeClass().isWidthAtLeastBreakpoint(1000)
}

@Composable
fun isCompact(): Boolean {
    return currentWindowSizeClass().minWidthDp < WIDTH_DP_EXPANDED_LOWER_BOUND
}
