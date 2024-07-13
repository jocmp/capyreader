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
import com.jocmp.capy.articles.ExtractedContent

@Composable
fun ArticleView(
    article: Article?,
    webViewNavigator: WebViewNavigator,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    val context = LocalContext.current
    val webViewState = rememberSaveableWebViewState(key = article?.id)
    val templateColors = articleTemplateColors()

    val buildHTML = { content: ExtractedContent ->
        article?.let {
            ArticleRenderer.render(
                article,
                extractedContent = content,
                templateColors = templateColors,
                context = context
            )
        }
    }

    val renderHTML = { content: ExtractedContent ->
        val html = buildHTML(content)
        html?.let { webViewNavigator.loadHtml(html) }
    }

    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            renderHTML(content)
        }
    )
    val extractedContent = extractedContentState.content

    fun onToggleExtractContent() {
        article ?: return

        if (extractedContent.isComplete) {
            extractedContentState.reset()
            renderHTML(ExtractedContent())
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
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    BackHandler(article != null) {
        webViewNavigator.clearView()
        onBackPressed()
    }

    LaunchedEffect(webViewNavigator) {
        val html = buildHTML(extractedContent)

        if (!html.isNullOrBlank() && webViewState.viewState == null) {
            webViewNavigator.loadHtml(html)
        }
    }

    LaunchedEffect(templateColors) {
        webViewState.webView?.let { webView ->
            updateStyleVariables(webView, templateColors)
        }
    }
}
