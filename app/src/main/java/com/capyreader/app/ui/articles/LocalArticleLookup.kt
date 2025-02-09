package com.capyreader.app.ui.articles

import androidx.compose.runtime.compositionLocalOf

val LocalArticleLookup = compositionLocalOf { ArticleLookup() }

data class ArticleLookup(
    val findIndex: (articleID: String) -> Int = { -1 }
)
