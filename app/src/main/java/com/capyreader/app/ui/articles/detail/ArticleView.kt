package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

            Column(
                Modifier.fillMaxSize()
            ) {
                WebView(
                    state = webViewState,
                    navigator = webViewNavigator,
                    onNavigateToMedia = onNavigateToMedia,
                    modifier = Modifier.fillMaxSize(),
                )
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
                val html = renderer.fetchCached(article)

                if (html.isNotBlank()) {
                    webViewNavigator.loadHtml(html)
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
    }

    ArticleTemplateColorListener(webView = webViewState.webView, templateColors = templateColors)

    ArticleStyleListener(webView = webViewState.webView)
}
