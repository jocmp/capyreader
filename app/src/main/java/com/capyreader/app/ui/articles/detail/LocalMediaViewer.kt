package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.compositionLocalOf

val LocalMediaViewer = compositionLocalOf { MediaViewer() }

data class MediaViewer(
    val open: (url: String) -> Unit = {}
)
