package com.capyreader.app.ui.articles.detail

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.pullrefresh.PullRefresh
import com.capyreader.app.ui.components.pullrefresh.rememberSwipeRefreshState
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun ArticleView(
    article: Article?,
    webViewNavigator: WebViewNavigator,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onNavigateToMedia: (url: String) -> Unit,
    enableBackHandler: Boolean = false
) {
    val articleID = article?.id
    val templateColors = articleTemplateColors()
    val colors = templateColors.asMap()
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val byline = article?.byline(context = LocalContext.current).orEmpty()
    var isRefreshing by remember { mutableStateOf(false) }

    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            article?.let {
                webViewNavigator.loadHtml(
                    renderer.render(
                        article,
                        byline = byline,
                        extractedContent = content,
                        colors = colors
                    )
                )
            }
        }
    )

    val clearWebView = {
        webViewNavigator.clearView()
        renderer.clear()
    }

    val extractedContent = extractedContentState.content

    fun onToggleExtractContent() {
        article ?: return

        if (extractedContent.isComplete) {
            webViewNavigator.loadHtml(renderer.render(article, byline = byline, colors = colors))
            extractedContentState.reset()
        } else if (!extractedContent.isLoading) {
            extractedContentState.fetch()
        }
    }

    Scaffold(
        topBar = {
            ArticleTopBar(
                article = article,
                extractedContent = extractedContent,
                onToggleExtractContent = ::onToggleExtractContent,
                onToggleRead = onToggleRead,
                onToggleStar = onToggleStar,
                onClose = onBackPressed,
                onStyleUpdate = {
                    renderer.clear()
                }
            )
        }
    ) { innerPadding ->

        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (article == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = 64.dp)

                        .fillMaxSize()
                ) {
                    CapyPlaceholder()
                }
            }

            PullRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = { Log.d("ArticleView", "ArticleView: done pulled") }
            ) {
                PullRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    indicatorAlignment = Alignment.BottomCenter,
                    onRefresh = { Log.d("ArticleView", "ArticleView: done pulled") }
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Column {
                            WebView(
                                state = webViewState,
                                navigator = webViewNavigator,
                                onNavigateToMedia = onNavigateToMedia,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        }
    }

    BackHandler(enableBackHandler && article != null) {
        onBackPressed()
    }

    LaunchedEffect(articleID) {
        launch(Dispatchers.IO) {
            if (article == null) {
                clearWebView()
            } else {
                if (extractedContent.requestShow) {
                    extractedContentState.fetch()
                } else {
                    val rendered = renderer.render(article, byline = byline, colors = colors)
                    webViewNavigator.loadHtml(rendered)
                }
            }
        }
    }

    ArticleStyleListener(webView = webViewState.webView)
}
