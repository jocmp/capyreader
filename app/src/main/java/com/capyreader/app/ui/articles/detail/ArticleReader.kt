package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewState
import com.jocmp.capy.Article

@Composable
fun ArticleReader(
    article: Article,
    scrollState: ScrollState,
    webViewState: WebViewState,
) {
    var lastScrollY by rememberSaveable { mutableIntStateOf(0) }

    ColumnScrollbar(state = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            WebView(
                state = webViewState,
                onDispose = {
                    lastScrollY = scrollState.value
                },
            )
            Spacer(modifier = Modifier.height(150.dp))
        }
    }

    LaunchedEffect(article.id, article.content) {
        webViewState.loadHtml(article)
    }

    LaunchedEffect(lastScrollY, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY > 0) {
            scrollState.scrollTo(lastScrollY)
            lastScrollY = 0
        }
    }

    ArticleStyleListener(webView = webViewState.webView)

    DisposableEffect(article.id) {
        onDispose {
            webViewState.reset()
        }
    }
}
