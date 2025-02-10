package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import com.capyreader.app.common.Media
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.rememberWebViewState
import com.jocmp.capy.logging.CapyLog

@Composable
fun ContentWebView(
    html: String,
    onNavigateToMedia: (media: Media) -> Unit,
    origin: String?,
    onRelease: () -> Unit,
) {
    val webView = rememberWebViewState(
        onNavigateToMedia = onNavigateToMedia,
    )

    WebView(
        webView = webView,
        update = {
            CapyLog.info(
                "update",
                mapOf("origin" to origin, "html_hash" to html.hashCode().toString())
            )
            it.loadDataWithBaseURL(
                origin,
                html,
                null,
                "UTF-8",
                null,
            )
        },
        onRelease = {
            onRelease()
        },
    )

    ArticleStyleListener(webView = webView)
}
