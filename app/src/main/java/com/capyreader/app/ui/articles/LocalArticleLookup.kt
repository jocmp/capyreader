package com.capyreader.app.ui.articles

import androidx.compose.runtime.compositionLocalOf
import com.jocmp.capy.ArticlePages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

val LocalArticleLookup = compositionLocalOf { ArticleLookup() }

data class ArticleLookup(
    val findArticlePages: (articleID: String) -> Flow<ArticlePages?> = { emptyFlow() }
)
