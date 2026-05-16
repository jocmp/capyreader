package com.capyreader.app.ui.articles.feeds

import androidx.compose.runtime.compositionLocalOf

val LocalFeedActions = compositionLocalOf { FeedActions() }

data class FeedActions(
    val updateOpenInBrowser: (feedID: String, openInBrowser: Boolean) -> Unit = { _, _ -> },
    val removeFeed: (feedID: String) -> Unit = {},
    val toggleUnreadBadge: (feedID: String, show: Boolean) -> Unit = { _, _ -> },
    val reloadIcon: (feedID: String) -> Unit = {},
    val toggleCacheOffline: (feedID: String, enabled: Boolean) -> Unit = { _, _ -> },
)
