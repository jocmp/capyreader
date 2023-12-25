package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.jocmp.basil.Article
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.rememberWebViewStateWithHTMLData

@Composable
fun ArticleView(
    article: Article?,
    onBackPressed: () -> Unit
) {
    val state = rememberWebViewStateWithHTMLData(article?.contentHTML ?: "<div />")

    WebView(state)

    BackHandler(article != null) {
        onBackPressed()
    }
}
