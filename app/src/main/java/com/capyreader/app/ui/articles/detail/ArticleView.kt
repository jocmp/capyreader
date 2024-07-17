package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.capyreader.app.ui.components.rememberWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer

@Composable
fun ArticleView(
    article: Article?,
    webViewNavigator: WebViewNavigator,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    val articleID = article?.id
    val context = LocalContext.current
    val templateColors = articleTemplateColors()
    val (initialized, setInitialized) = rememberSaveable(articleID) {
        mutableStateOf(false)
    }

    val renderer = ArticleRenderer(context = context, colors = templateColors.asMap())
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            article?.let {
                webViewNavigator.loadHtml(renderer.render(article, content))
            }
        }
    )

    val extractedContent = extractedContentState.content

    fun onToggleExtractContent() {
        article ?: return

        if (extractedContent.isComplete) {
            webViewNavigator.loadHtml(renderer.render(article))
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

    LaunchedEffect(articleID) {
        if (articleID == null) {
            return@LaunchedEffect
        }

        if (extractedContent.requestShow) {
            extractedContentState.fetch()
        } else {
            webViewNavigator.loadHtml(renderer.render(article))
        }

        setInitialized(true)
    }

    LaunchedEffect(templateColors) {
        webViewState.webView?.let { webView ->
            updateStyleVariables(webView, templateColors)
        }
    }
}
