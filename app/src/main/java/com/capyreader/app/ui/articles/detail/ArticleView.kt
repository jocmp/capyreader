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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.pullrefresh.PullRefresh
import com.capyreader.app.ui.components.pullrefresh.rememberSwipeRefreshState
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: Flow<PagingData<Article>>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (index: Int, id: String) -> Unit
) {
    val snapshotList = articles.collectAsLazyPagingItems().itemSnapshotList
    val showBars = true // canShowTopBar(webViewState)
    val fullContent = LocalFullContent.current

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
                SwipeRefresh(
                    article,
                    articles = snapshotList,
                    onRequestArticle = onRequestArticle
                ) {
                    ArticleReader(
                        article = article
                    )
                }
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

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
fun SwipeRefresh(
    article: Article,
    onRequestArticle: (index: Int, id: String) -> Unit,
    articles: List<Article?>,
    content: @Composable () -> Unit,
) {
    val relations = remember(article, articles) { ArticleRelations.from(article, articles) }

    val selectArticle = { index: Int ->
        articles.getOrNull(index)?.let { related ->
            onRequestArticle(index, related.id)
        }
    }

    PullRefresh(
        state = rememberSwipeRefreshState(),
        onRefresh = { selectArticle(relations.previous) }
    ) {
        PullRefresh(
            state = rememberSwipeRefreshState(),
            indicatorAlignment = Alignment.BottomCenter,
            onRefresh = { selectArticle(relations.next) }
        ) {
            content()
        }
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
