package com.capyreader.app.ui.articles.detail

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.TextAlignment
import org.koin.compose.koinInject

@Composable
fun ArticleStyleListener(webView: WebView?, appPreferences: AppPreferences = koinInject()) {
    val textSize by appPreferences.readerOptions.fontSize.collectChangesWithDefault()
    val fontFamily by appPreferences.readerOptions.fontFamily.collectChangesWithDefault()
    val appTheme by appPreferences.appTheme.collectChangesWithDefault()
    val themeMode by appPreferences.themeMode.collectChangesWithDefault()
    val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithDefault()
    val titleFontSize by appPreferences.readerOptions.titleFontSize.collectChangesWithDefault()
    val titleTextAlignment by appPreferences.readerOptions.titleTextAlignment.collectChangesWithDefault()
    val titleFollowsBodyFont by appPreferences.readerOptions.titleFollowsBodyFont.collectChangesWithDefault()

    val colors = articleTemplateColors()

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

    LaunchedEffect(appTheme, themeMode, pureBlackDarkMode) {
        if (webView != null) {
            updateThemeColors(webView, colors)
        }
    }

    LaunchedEffect(titleFontSize) {
        if (webView != null) {
            updateTitleFontSize(webView, titleFontSize)
        }
    }

    LaunchedEffect(titleTextAlignment) {
        if (webView != null) {
            updateTitleAlignment(webView, titleTextAlignment)
        }
    }

    LaunchedEffect(titleFollowsBodyFont, fontFamily) {
        if (webView != null) {
            val titleFont = if (titleFollowsBodyFont) fontFamily else FontOption.SYSTEM_DEFAULT
            updateTitleFontFamily(webView, titleFont)
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

private fun updateThemeColors(webView: WebView, colors: Map<String, String>) {
    val updatedColors = colors.map { (key, value) ->
        "document.documentElement.style.setProperty('--${key.replace('_', '-')}', '$value');"
    }.joinToString("\n")

    webView.evaluateJavascript(
        """
        (function() {
          $updatedColors
        })();
        """.trimIndent()
    ) {
    }
}

private fun Color.toHTMLColor(): String {
    val hex = Integer.toHexString(toArgb()).takeLast(6)
    return "#${hex.uppercase()}"
}

private fun updateTitleFontSize(webView: WebView, fontSize: Int) {
    webView.evaluateJavascript(
        """
        (function() {
          document.documentElement.style.setProperty("--article-title-font-size", "${fontSize}px");
        })();
        """.trimIndent()
    ) {}
}

private fun updateTitleAlignment(webView: WebView, alignment: TextAlignment) {
    webView.evaluateJavascript(
        """
        (function() {
          document.documentElement.style.setProperty("--article-title-text-align", "${alignment.toCSS}");
        })();
        """.trimIndent()
    ) {}
}

private fun updateTitleFontFamily(webView: WebView, fontOption: FontOption) {
    webView.evaluateJavascript(
        """
        (function() {
          let slug = "${fontOption.slug}";
          let articleTitle = document.getElementsByClassName("article__title")[0];

          if (articleTitle) {
            const classes = articleTitle.className.split(" ").filter(c => !c.startsWith("article__title--font"));
            articleTitle.className = classes.join(" ").trim() + " article__title--font-" + slug;
          }
        })();
        """.trimIndent()
    ) {}
}
