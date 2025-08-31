package com.capyreader.app.ui.articles.detail

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.articles.FontOption
import org.koin.compose.koinInject

@Composable
fun ArticleStyleListener(webView: WebView?, appPreferences: AppPreferences = koinInject()) {
    val textSize by appPreferences.readerOptions.fontSize.collectChangesWithDefault()
    val fontFamily by appPreferences.readerOptions.fontFamily.collectChangesWithDefault()

    LaunchedEffect(fontFamily) {
        if (webView != null) {
            updateFontFamily(webView, fontFamily)
        }
    }

    LaunchedEffect(textSize) {
        if (webView != null) {
            updateFontSize(webView, textSize)
        }
    }
}

private fun updateFontSize(webView: WebView, fontSize: Int) {
    webView.evaluateJavascript(
        """
        (function() {
          document.documentElement.style.setProperty("--article-font-size", "${fontSize}px");
        })();
        """.trimIndent()
    ) {
    }
}

private fun updateFontFamily(webView: WebView, fontOption: FontOption) {
    webView.evaluateJavascript(
        """
        (function() {
          let slug = "${fontOption.slug}";
          let articleBody = document.getElementsByClassName("article__body")[0];

          if (articleBody) {
            const classes = articleBody.className.split(" ").filter(c => !c.startsWith("article__body--font"));

            articleBody.className = classes.join(" ").trim() + " article__body--font-" + slug;
          }
        })();
        """.trimIndent()
    ) {
    }
}
