package com.capyreader.app.ui.articles.detail

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.pullrefresh.PullRefresh
import com.capyreader.app.ui.components.pullrefresh.rememberSwipeRefreshState
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: List<Article?>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (index: Int, id: String) -> Unit
) {
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
                    articles = articles,
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
    val articlePosition = remember(article.id, articles.size) { ArticleRelations.from(article, articles) }

    val selectArticle = { index: Int ->
        articles.getOrNull(index)?.let { related ->
            onRequestArticle(index, related.id)
        }
    }

    PullRefresh(
        swipeEnabled = articlePosition.hasPrevious(),
        state = rememberSwipeRefreshState(),
        onRefresh = { selectArticle(articlePosition.previous) }
    ) {
        PullRefresh(
            swipeEnabled = articlePosition.hasNext(),
            state = rememberSwipeRefreshState(),
            indicatorAlignment = Alignment.BottomCenter,
            onRefresh = { selectArticle(articlePosition.next) }
        ) {
            key(article.id) {
                content()
            }
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
