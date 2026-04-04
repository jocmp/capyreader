package com.capyreader.app.ui.settings.filters

import androidx.compose.runtime.compositionLocalOf

val LocalFilterKeywords = compositionLocalOf { FilterKeywords() }

data class FilterKeywords(
    val add: (keyword: String) -> Unit = {},
    val remove: (keyword: String) -> Unit = {},
    val keywords: List<String> = emptyList(),
)
