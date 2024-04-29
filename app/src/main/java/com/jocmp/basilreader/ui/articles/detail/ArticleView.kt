package com.jocmp.basilreader.ui.articles.detail

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.jocmp.basil.Article
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.EmptyView
import com.jocmp.basilreader.ui.components.LoadingState
import com.jocmp.basilreader.ui.components.WebContent
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.WebViewNavigator
import com.jocmp.basilreader.ui.components.WebViewState
import com.jocmp.basilreader.ui.components.rememberSaveableWebViewState

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

    Scaffold(
        topBar = {
            ArticleTopBar(
                article = article,
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                    )
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
        val html = ArticleRenderer.render(article, templateColors, context)

        if (html.isNotBlank() && webViewState.viewState == null) {
            webViewNavigator.loadHtml(html)
        }
    }

    LaunchedEffect(templateColors) {
        webViewState.webView?.let { webView ->
            updateStyleVariables(webView, templateColors)
        }
    }
}
