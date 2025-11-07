package com.capyreader.app.ui.articles.detail

import android.webkit.WebView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.articles.FontOption
import org.koin.compose.koinInject

@Composable
fun ArticleStyleListener(webView: WebView?, appPreferences: AppPreferences = koinInject()) {
    val textSize by appPreferences.readerOptions.fontSize.collectChangesWithDefault()
    val fontFamily by appPreferences.readerOptions.fontFamily.collectChangesWithDefault()
    val appTheme by appPreferences.appTheme.collectChangesWithDefault()
    val themeMode by appPreferences.themeMode.collectChangesWithDefault()
    val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithDefault()

    val colorScheme = MaterialTheme.colorScheme

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
            updateThemeColors(webView, colorScheme)
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

private fun updateThemeColors(webView: WebView, colorScheme: androidx.compose.material3.ColorScheme) {
    val colors = mapOf(
        "color-on-primary-container" to colorScheme.onPrimaryContainer.toHTMLColor(),
        "color-on-surface" to colorScheme.onSurface.toHTMLColor(),
        "color-on-surface-variant" to colorScheme.onSurfaceVariant.toHTMLColor(),
        "color-primary" to colorScheme.primary.toHTMLColor(),
        "color-primary-container" to colorScheme.primaryContainer.toHTMLColor(),
        "color-surface" to colorScheme.surface.toHTMLColor(),
        "color-secondary" to colorScheme.secondary.toHTMLColor(),
        "color-surface-container-highest" to colorScheme.surfaceContainerHighest.toHTMLColor(),
        "color-surface-variant" to colorScheme.surfaceVariant.toHTMLColor(),
        "color-surface-container" to colorScheme.surfaceContainer.toHTMLColor()
    )

    val jsColorUpdates = colors.map { (key, value) ->
        "document.documentElement.style.setProperty('--$key', '$value');"
    }.joinToString("\n")

    webView.evaluateJavascript(
        """
        (function() {
          $jsColorUpdates
        })();
        """.trimIndent()
    ) {
    }
}

private fun Color.toHTMLColor(): String {
    val hex = Integer.toHexString(toArgb()).takeLast(6)
    return "#${hex.uppercase()}"
}
