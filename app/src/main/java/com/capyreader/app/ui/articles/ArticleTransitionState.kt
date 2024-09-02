package com.capyreader.app.ui.articles

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

@Stable
data class ArticleTransitionState(
    val isAnimating: Boolean
)

val LocalArticleTransitionState = compositionLocalOf { DefaultArticleTransitionState }

private val DefaultArticleTransitionState = ArticleTransitionState(isAnimating = false)
