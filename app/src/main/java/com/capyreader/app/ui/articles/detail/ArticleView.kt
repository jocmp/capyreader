package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: Flow<PagingData<Article>>,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    selectArticle: (index: Int, id: String) -> Unit
) {
    val snapshotList = articles.collectAsLazyPagingItems().itemSnapshotList
    val relations = remember(article, snapshotList) { ArticleRelations.from(article, snapshotList) }
    val colors = articleTemplateColors()
    val byline = article.byline(context = LocalContext.current)
    val showBars = true // canShowTopBar(webViewState)
    val fullContent = LocalFullContent.current

    val selectPreviousArticle = {
        snapshotList.getOrNull(relations.previous)?.let { related ->
            selectArticle(relations.previous, related.id)
        }
    }

    val selectNextArticle = {
        snapshotList.getOrNull(relations.next)?.let { related ->
            selectArticle(relations.next, related.id)
        }
    }

    fun onToggleExtractContent() {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    Scaffold { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                key(article.id) {
                    ArticleReader(
                        article = article
                    )
                }
            }

            AnimatedVisibility(
                visible = showBars,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ArticleTopBar(
                    article = article,
                    onToggleExtractContent = ::onToggleExtractContent,
                    onToggleRead = onToggleRead,
                    onToggleStar = onToggleStar,
                    onClose = onBackPressed
                )
            }
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
fun canShowTopBar(
    webViewState: WebViewState,
    appPreferences: AppPreferences = koinInject(),
): Boolean {
    val pinTopBar by appPreferences.pinArticleTopBar
        .stateIn(rememberCoroutineScope())
        .collectAsState()

    return pinTopBar
}
