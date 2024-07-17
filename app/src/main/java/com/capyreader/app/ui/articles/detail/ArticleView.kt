package com.capyreader.app.ui.articles.detail

import android.util.Log
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
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer

private const val TAG = "ArticleView"

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
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val templateColors = articleTemplateColors()
    val (initialized, setInit) = rememberSaveable(articleID) {
        mutableStateOf(false)
    }

    val renderer = ArticleRenderer(context = context, colors = templateColors.asMap())

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
        if (articleID == null || initialized) {
            Log.d(TAG, "ArticleView: launch1 null articleID or initialized=${initialized}")
        } else {
            if (extractedContent.showOnLoad) {
                Log.d(TAG, "ArticleView: launch1 showOnLoad")
                webViewNavigator.clearView()
                extractedContentState.fetch()
            } else {
                Log.d(TAG, "ArticleView: launch1 render default")
                webViewNavigator.loadHtml(renderer.render(article))
            }
            setInit(true)
        }
    }

    // https://github.com/google/accompanist/pull/1557
    LaunchedEffect(webViewNavigator) {
        if (webViewState.viewState != null || article == null) {
            Log.d(
                TAG,
                "ArticleView: launch2 early return viewStatePresent=${webViewState.viewState != null} article=${article?.id}"
            )
            return@LaunchedEffect
        }

        if (extractedContent.showByDefault) {
            Log.d(TAG, "ArticleView: launch2 showByDefault")
            extractedContentState.fetch()
        } else {
            Log.d(TAG, "ArticleView: launch2 loadHtml")
            webViewNavigator.loadHtml(renderer.render(article))
        }
    }

    LaunchedEffect(templateColors) {
        webViewState.webView?.let { webView ->
            updateStyleVariables(webView, templateColors)
        }
    }
}
