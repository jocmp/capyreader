package com.capyreader.app.ui.articles.reader

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp

val LocalReaderPageHeight = compositionLocalOf<Dp?> { null }

data class PageInsets(val top: Float, val bottom: Float)
