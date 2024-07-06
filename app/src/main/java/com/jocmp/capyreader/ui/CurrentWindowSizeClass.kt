package com.jocmp.capyreader.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass.Companion.EXPANDED

@Composable
fun currentWindowSizeClass() = currentWindowAdaptiveInfo().windowSizeClass

@Composable
fun isCompact(): Boolean {
    return currentWindowSizeClass().windowWidthSizeClass != EXPANDED
}
