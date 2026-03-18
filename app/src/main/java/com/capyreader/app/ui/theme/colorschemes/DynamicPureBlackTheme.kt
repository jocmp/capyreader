package com.capyreader.app.ui.theme.colorschemes

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun ColorScheme.applyPureBlack(pureBlack: Boolean): ColorScheme {
    if (!pureBlack) return this

    return copy(
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceVariant = Color.Black,
        onSurfaceVariant = Color.White,
        outline = Color.White,
        outlineVariant = Color.White,
        scrim = Color.Black,
        inverseSurface = Color.White,
        inverseOnSurface = Color.Black,
        surfaceDim = Color.Black,
        surfaceBright = Color.White,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color.Black,
        surfaceContainer = Color.Black,
        surfaceContainerHigh = Color.Black,
        surfaceContainerHighest = Color.Black,
    )
}
