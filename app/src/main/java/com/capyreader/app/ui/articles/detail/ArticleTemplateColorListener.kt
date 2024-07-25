package com.capyreader.app.ui.articles.detail

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jocmp.capy.articles.TemplateColors

@Composable
fun ArticleTemplateColorListener(webView: WebView?, templateColors: TemplateColors) {
    LaunchedEffect(templateColors) {
        if (webView != null) {
            updateStyleVariables(webView, templateColors)
        }
    }
}

/**
 * Update CSS variables dynamically to avoid a full-page reload.
 *
 * A "prefer-color-scheme" approach is not sufficient since variables may change completely
 * based on the dynamic system theme.
 */
fun updateStyleVariables(webView: WebView, templateColors: TemplateColors) {
    webView.evaluateJavascript(
        """
        (function() {
          var colors = ${templateColors.toJSON()}

          for (const [property, value] of Object.entries(colors)) {
            document.documentElement.style.setProperty(property, value);
          }
        })();
        """.trimIndent()
    ) {
    }
}
