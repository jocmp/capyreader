package com.capyreader.app.ui.articles.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.articles.FontOption
import com.jocmp.hyperview.HtmlStyle
import org.koin.compose.koinInject

/**
 * Builds an [HtmlStyle] that mirrors the article CSS used by the WebView path:
 *  - body font + size taken from `readerOptions.fontFamily` / `fontSize`
 *  - line-height 1.6em (matching `--article-line-height`)
 *  - blockquote: 2dp bar, outlineVariant
 *  - code: surfaceContainerHighest background
 *  - figure caption: 0.75x body size
 */
@Composable
fun rememberArticleHtmlStyle(
    appPreferences: AppPreferences = koinInject(),
): HtmlStyle {
    val fontOption by appPreferences.readerOptions.fontFamily.collectChangesWithDefault()
    val fontSize by appPreferences.readerOptions.fontSize.collectChangesWithDefault()

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val bodyFontFamily = remember(fontOption) { fontOption.toFontFamily() }

    return remember(fontOption, fontSize, colorScheme) {
        val bodySp = fontSize.sp
        val body = typography.bodyLarge.copy(
            fontFamily = bodyFontFamily,
            fontSize = bodySp,
            lineHeight = (fontSize * 1.6f).sp,
            color = colorScheme.onSurface,
        )
        HtmlStyle(
            body = body,
            heading = { level ->
                val scale = when (level) {
                    1 -> 1.8f
                    2 -> 1.5f
                    3 -> 1.25f
                    4 -> 1.1f
                    5 -> 1.0f
                    else -> 0.9f
                }
                body.copy(
                    fontSize = (fontSize * scale).sp,
                    lineHeight = (fontSize * scale * 1.25f).sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            link = body.copy(color = colorScheme.primary),
            code = body.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = (fontSize * 0.9f).sp,
            ),
            codeBlockBackground = colorScheme.surfaceContainerHighest,
            blockquoteBar = colorScheme.outlineVariant,
            blockquoteBarWidth = 2.dp,
            blockSpacing = (fontSize * 0.85f).dp,
            listIndent = (fontSize * 1.25f).dp,
            horizontalRuleColor = colorScheme.outlineVariant,
        )
    }
}

private fun FontOption.toFontFamily(): FontFamily = when (this) {
    FontOption.SYSTEM_DEFAULT -> FontFamily.SansSerif
    FontOption.ATKINSON_HYPERLEGIBLE -> FontFamily(Font(R.font.atkinson_hyperlegible))
    FontOption.INTER -> FontFamily(Font(R.font.inter))
    FontOption.JOST -> FontFamily(Font(R.font.jost))
    FontOption.LITERATA -> FontFamily(Font(R.font.literata))
    FontOption.POPPINS -> FontFamily(Font(R.font.poppins))
    FontOption.VOLLKORN -> FontFamily(Font(R.font.vollkorn))
}

@Suppress("UnusedReceiverParameter")
internal val ArticleHtmlMaxWidth = 640.dp
