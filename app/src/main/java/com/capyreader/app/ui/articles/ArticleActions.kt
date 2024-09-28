package com.capyreader.app.ui.articles

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

val LocalArticleActions = compositionLocalOf { ArticleActions() }

@Stable
data class ArticleActions(
    val markRead: (articleID: String) -> Unit = {},
    val star: (articleID: String) -> Unit = {},
    val markUnread: (articleID: String) -> Unit = {},
    val unstar: (articleID: String) -> Unit = {},
)
