package com.jocmp.capyreader.ui

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf


val LocalWindowWidth = compositionLocalOf {
    WindowWidthSizeClass.Compact
}

val WindowWidthSizeClass.isCompact
    get() = this == WindowWidthSizeClass.Compact
