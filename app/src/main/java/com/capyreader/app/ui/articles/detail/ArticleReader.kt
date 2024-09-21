package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.Scrollbar
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.rememberWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.compose.koinInject

@Composable
fun ArticleReader(
    article: Article,
    scrollState: ScrollState,
    renderer: ArticleRenderer = koinInject(),
) {
    val mediaViewer = LocalMediaViewer.current
    val colors = articleTemplateColors()
    var lastScrollY by rememberSaveable { mutableIntStateOf(0) }
    val webViewState = rememberWebViewState()
    val byline = article.byline(context = LocalContext.current)

    fun render(): String {
        return renderer.render(
            article,
            byline = byline,
            colors = colors
        )
    }

    Scrollbar(scrollState = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            WebView(
                state = webViewState,
                onNavigateToMedia = {
                    mediaViewer.open(it)
                },
                onDispose = {
                    lastScrollY = scrollState.value
                },
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    LaunchedEffect(article.content) {
        webViewState.loadHtml(render())
    }

    LaunchedEffect(lastScrollY, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY > 0) {
            scrollState.scrollTo(lastScrollY)
            lastScrollY = 0
        }
    }

    ArticleStyleListener(webView = webViewState.webView)
}
