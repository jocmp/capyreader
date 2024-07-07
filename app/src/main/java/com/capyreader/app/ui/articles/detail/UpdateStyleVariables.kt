package com.capyreader.app.ui.articles.detail

import android.webkit.WebView

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
