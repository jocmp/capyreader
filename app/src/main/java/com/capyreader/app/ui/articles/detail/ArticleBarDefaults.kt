package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object ArticleBarDefaults {
    val TopBarHeight = 64.dp
    val FloatingToolbarHeight = 64.dp
    val FloatingToolbarBottomGap = 12.dp
    val BottomBarHeight = FloatingToolbarHeight + FloatingToolbarBottomGap

    val topInset: Dp
        @Composable get() = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top)
            .asPaddingValues()
            .calculateTopPadding()

    val topBarOffset: Dp
        @Composable get() = topInset + TopBarHeight
}
