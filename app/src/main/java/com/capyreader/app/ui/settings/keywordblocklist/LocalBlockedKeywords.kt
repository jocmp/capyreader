package com.capyreader.app.ui.settings.keywordblocklist

import androidx.compose.runtime.compositionLocalOf

val LocalBlockedKeywords = compositionLocalOf { BlockedKeywords() }

data class BlockedKeywords(
    val add: (keyword: String) -> Unit = {},
    val remove: (keyword: String) -> Unit = {},
    val keywords: List<String> = emptyList(),
)
