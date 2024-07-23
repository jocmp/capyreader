package com.capyreader.app.ui.articles.detail

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.articles.FontFamily
import org.koin.compose.koinInject

@Composable
fun ArticleFontFamilyListener(webView: WebView?, appPreferences: AppPreferences = koinInject()) {
    val fontFamily by appPreferences.fontFamily
        .changes()
        .collectAsState(initial = appPreferences.fontFamily.get())

    LaunchedEffect(fontFamily) {
        if (webView != null) {
            updateFontFamily(webView, fontFamily)
        }
    }
}

private fun updateFontFamily(webView: WebView, fontFamily: FontFamily) {
    webView.evaluateJavascript(
        """
        (function() {         
          let slug = "${fontFamily.slug}";
          let articleBody = document.getElementsByClassName("article__body")[0];
          const classes = articleBody.className.split(" ").filter(c => !c.startsWith("article__body--font"));
          
          articleBody.className = classes.join(" ").trim() + " article__body--font-" + slug;
        })();
        """.trimIndent()
    ) {
    }
}
