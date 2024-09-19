package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.rememberWebViewState
import com.capyreader.app.ui.components.rememberWebViewNavigator
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import org.koin.compose.koinInject

@Composable
fun ArticleReader(
    article: Article,
    renderer: ArticleRenderer = koinInject(),
    selectArticle: (index: Int, id: String) -> Unit,
) {
    val mediaViewer = LocalMediaViewer.current
    val webViewNavigator = rememberWebViewNavigator()
    val articleID = article.id
    val colors = articleTemplateColors()
    val webViewState = rememberWebViewState()
    val byline = article.byline(context = LocalContext.current)

    fun render(): String {
        return renderer.render(
            article,
            byline = byline,
            colors = colors
        )
    }

    WebView(
        state = webViewState,
        navigator = webViewNavigator,
        onNavigateToMedia = { mediaViewer.open(it) },
    )

    LaunchedEffect(article.content) {
        webViewNavigator.loadHtml(render())
    }

    ArticleStyleListener(webView = webViewState.webView)
}
