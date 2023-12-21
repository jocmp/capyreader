package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import com.jocmp.basil.db.Articles
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.rememberWebViewStateWithHTMLData

@Composable
fun ArticleView(article: Articles) {
    val state = rememberWebViewStateWithHTMLData(article.content_html ?: "<div>nothing</div>")

    WebView(state)
}
