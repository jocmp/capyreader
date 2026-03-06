package com.capyreader.app.ui.articles.feeds

import androidx.compose.runtime.compositionLocalOf

val LocalSavedSearchActions = compositionLocalOf { SavedSearchActions() }

data class SavedSearchActions(
    val toggleUnreadBadge: (id: String, show: Boolean) -> Unit = { _, _ -> },
)
