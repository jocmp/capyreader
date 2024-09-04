package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.capyreader.app.ui.isCompact
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
                onClose = onBackPressed
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (article == null && !isCompact()) {
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
                article?.let {
                    Column(
                        Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = article.title,
                            fontSize = 24.sp,
                            lineHeight = (28.8).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
