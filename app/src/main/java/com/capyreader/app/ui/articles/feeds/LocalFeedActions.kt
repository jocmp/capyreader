package com.capyreader.app.ui.articles.feeds

import androidx.compose.runtime.compositionLocalOf

val LocalFeedActions = compositionLocalOf { FeedActions() }

data class FeedActions(
    val updateOpenInBrowser: (feedID: String, openInBrowser: Boolean) -> Unit = { _, _ -> },
    val removeFeed: (feedID: String) -> Unit = {},
)
