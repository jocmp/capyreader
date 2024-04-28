package com.jocmp.basilreader.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jocmp.basil.Article
import com.jocmp.basilreader.ui.components.EmptyView
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.WebViewNavigator
import com.jocmp.basilreader.ui.components.WebViewState

@Composable
fun ArticleView(
    article: Article?,
    webViewState: WebViewState,
    webViewNavigator: WebViewNavigator,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {

    if (article != null) {
        ArticleLoadedView(
            article = article,
            onToggleRead = onToggleRead,
            onToggleStar = onToggleStar,
            webViewState = webViewState,
            navigator = webViewNavigator,
            onClose = onBackPressed
        )
    } else {
        EmptyView()
    }


    BackHandler(article != null) {
        onBackPressed()
    }
}

@Composable
fun ArticleLoadedView(
    article: Article,
    webViewState: WebViewState,
    navigator: WebViewNavigator,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            ArticleTopBar(
                article = article,
                onToggleRead = onToggleRead,
                onToggleStar = onToggleStar,
                onClose = onClose
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            WebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
