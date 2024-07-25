package com.capyreader.app.ui.articles.detail

import android.util.Log
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.articles.TextSize
import org.koin.compose.koinInject

@Composable
fun ArticleTextSizeListener(webView: WebView?, appPreferences: AppPreferences = koinInject()) {
    val textSize by appPreferences.textSize
        .changes()
        .collectAsState(initial = appPreferences.textSize.get())

    LaunchedEffect(textSize) {
        if (webView != null) {
            updateTextSize(webView, textSize)
        }
    }
}

private fun updateTextSize(webView: WebView, textSize: TextSize) {
    webView.evaluateJavascript(
        """
        (function() {         
          let slug = "${textSize.slug}";
          let articleBody = document.getElementsByClassName("article__body")[0];
          const classes = articleBody.className.split(" ").filter(c => !c.startsWith("article__body--text-size"));
          
          articleBody.className = classes.join(" ").trim() + " article__body--text-size-" + slug;
        })();
        """.trimIndent()
    ) {
    }
}
