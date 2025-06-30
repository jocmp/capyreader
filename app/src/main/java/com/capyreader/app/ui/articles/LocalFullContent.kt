package com.capyreader.app.ui.articles

import androidx.compose.runtime.compositionLocalOf

val LocalFullContent = compositionLocalOf { FullContentFetcher() }

data class FullContentFetcher(
    val fetch: () -> Unit = {},
    val reset: () -> Unit = {}
)
