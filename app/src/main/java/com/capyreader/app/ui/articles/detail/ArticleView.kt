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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.articles.ArticleRelations
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article,
    articles: Flow<PagingData<Article>>,
    webViewNavigator: WebViewNavigator,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onNavigateToMedia: (url: String) -> Unit,
    enableBackHandler: Boolean = false,
    selectArticle: (index: Int, id: String) -> Unit
) {
    val snapshotList = articles.collectAsLazyPagingItems().itemSnapshotList
    val relations = remember(article, snapshotList) { ArticleRelations.from(article, snapshotList) }
    val articleID = article.id
    val colors = articleTemplateColors()
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val byline = article.byline(context = LocalContext.current)
    val showBars = canShowTopBar(webViewState)
    val fullContent = LocalFullContent.current

    fun render(): String {
        return renderer.render(
            article,
            byline = byline,
            colors = colors
        )
    }

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
            webViewNavigator.loadHtml(render())
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
            WebView(
                state = webViewState,
                navigator = webViewNavigator,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )

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

    LaunchedEffect(article.content) {
        webViewNavigator.loadHtml(render())
    }

    ArticleStyleListener(webView = webViewState.webView)
}

@Composable
fun canShowTopBar(
    webViewState: WebViewState,
    appPreferences: AppPreferences = koinInject(),
): Boolean {
    val pinTopBar by appPreferences.pinArticleTopBar
        .stateIn(rememberCoroutineScope())
        .collectAsState()

    return pinTopBar ||
            webViewState.scrollValue == 0 || webViewState.lastScrolledBackward
}
