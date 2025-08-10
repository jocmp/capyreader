package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.rememberArticlePagination
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel = koinViewModel(),
    onNavigateUp: () -> Boolean,
    onSelectArticle: (id: String) -> Unit,
) {
    val article = viewModel.article ?: return

    val pagination = rememberArticlePagination(
        article,
        onSelectArticle = { index, articleID ->
            onSelectArticle(articleID)
        }
    )

    ArticleView(
        article = article,
        pagination = pagination,
        onBackPressed = {
            onNavigateUp()
        },
        onToggleRead = {},
        onToggleStar = {},
        enableBackHandler = true,
        onScrollToArticle = {},
        onSelectMedia = {},
    )
}
