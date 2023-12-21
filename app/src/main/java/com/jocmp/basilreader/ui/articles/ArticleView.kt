package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.jocmp.basil.db.Articles
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.rememberWebViewStateWithHTMLData

@Composable
fun ArticleView(
    article: Articles?,
    onBackPressed: () -> Unit
) {
    val state = rememberWebViewStateWithHTMLData(article?.content_html ?: "<div />")

    WebView(state)

    BackHandler(article != null) {
        onBackPressed()
    }
}
