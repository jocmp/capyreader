package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jocmp.capy.ArticlePages
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.article.collectAsState(null)
    val article = state ?: return

    ArticleView(
        article = article,
        pagination = ArticlePagination(
            pages = ArticlePages(null, 0, null, 0),
            onSelectArticle = { _, _ -> }),
        onBackPressed = onBack,
        onToggleRead = {},
        onToggleStar = {},
        enableBackHandler = false,
        onNavigateToMedia = {}
    )
}