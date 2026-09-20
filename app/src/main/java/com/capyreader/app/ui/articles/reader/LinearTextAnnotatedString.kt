package com.capyreader.app.ui.articles.reader

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.isSpecified
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationBold
import com.jocmp.mallet.LinearTextAnnotationCode
import com.jocmp.mallet.LinearTextAnnotationFont
import com.jocmp.mallet.LinearTextAnnotationH1
import com.jocmp.mallet.LinearTextAnnotationH2
import com.jocmp.mallet.LinearTextAnnotationH3
import com.jocmp.mallet.LinearTextAnnotationH4
import com.jocmp.mallet.LinearTextAnnotationH5
import com.jocmp.mallet.LinearTextAnnotationH6
import com.jocmp.mallet.LinearTextAnnotationItalic
import com.jocmp.mallet.LinearTextAnnotationLink
import com.jocmp.mallet.LinearTextAnnotationMonospace
import com.jocmp.mallet.LinearTextAnnotationStrikethrough
import com.jocmp.mallet.LinearTextAnnotationSubscript
import com.jocmp.mallet.LinearTextAnnotationSuperscript
import com.jocmp.mallet.LinearTextAnnotationUnderline
import com.jocmp.mallet.LinearTextBlockStyle

val LinearText.headingScale: Float?
    get() = annotations.firstNotNullOfOrNull { annotation ->
        when (annotation.data) {
            LinearTextAnnotationH1 -> 2f
            LinearTextAnnotationH2 -> 1.5f
            LinearTextAnnotationH3 -> 1.17f
            LinearTextAnnotationH4 -> 1f
            LinearTextAnnotationH5 -> 0.83f
            LinearTextAnnotationH6 -> 0.67f
            else -> null
        }
    }

val LinearText.links: List<LinearTextAnnotation>
    get() = annotations.filter { it.data is LinearTextAnnotationLink }

@Composable
fun LinearText.toAnnotatedString(
    idToIndex: Map<String, Int>,
    onLinkClick: (url: String, elementIndex: Int?) -> Unit,
): AnnotatedString {
    val colors = MaterialTheme.colorScheme
    val baseSize = LocalTextStyle.current.fontSize
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline)
    )

    return buildAnnotatedString {
        append(text)

        annotations.forEach { annotation ->
            val link = annotation.data as? LinearTextAnnotationLink

            if (link != null) {
                val href = link.href

                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = href,
                        styles = linkStyles,
                        linkInteractionListener = {
                            onLinkClick(href, resolveAnchorIndex(href, idToIndex))
                        },
                    ),
                    start = annotation.start,
                    end = annotation.endExclusive,
                )

                return@forEach
            }

            val style = when (val data = annotation.data) {
                LinearTextAnnotationBold -> SpanStyle(fontWeight = FontWeight.Bold)
                LinearTextAnnotationItalic -> SpanStyle(fontStyle = FontStyle.Italic)
                LinearTextAnnotationUnderline -> SpanStyle(textDecoration = TextDecoration.Underline)
                LinearTextAnnotationStrikethrough -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                LinearTextAnnotationMonospace -> SpanStyle(fontFamily = FontFamily.Monospace)
                is LinearTextAnnotationLink -> null
                is LinearTextAnnotationFont -> SpanStyle(fontFamily = data.face.toFontFamily())
                LinearTextAnnotationCode -> inlineCodeStyle(colors.surfaceContainer)
                LinearTextAnnotationSubscript -> shiftedStyle(BaselineShift.Subscript, baseSize)
                LinearTextAnnotationSuperscript -> shiftedStyle(BaselineShift.Superscript, baseSize)
                LinearTextAnnotationH1,
                LinearTextAnnotationH2,
                LinearTextAnnotationH3,
                LinearTextAnnotationH4,
                LinearTextAnnotationH5,
                LinearTextAnnotationH6 -> null
            }

            if (style != null) {
                addStyle(style = style, start = annotation.start, end = annotation.endExclusive)
            }
        }
    }
}

private fun LinearText.inlineCodeStyle(background: androidx.compose.ui.graphics.Color): SpanStyle? {
    if (blockStyle == LinearTextBlockStyle.CODE_BLOCK) {
        return null
    }

    return SpanStyle(fontFamily = FontFamily.Monospace, background = background)
}

private fun shiftedStyle(shift: BaselineShift, baseSize: androidx.compose.ui.unit.TextUnit): SpanStyle {
    if (!baseSize.isSpecified) {
        return SpanStyle(baselineShift = shift)
    }

    return SpanStyle(baselineShift = shift, fontSize = baseSize * 0.75f)
}

private fun String.toFontFamily(): FontFamily? {
    return when (lowercase()) {
        "monospace" -> FontFamily.Monospace
        "serif" -> FontFamily.Serif
        "sans-serif" -> FontFamily.SansSerif
        "cursive" -> FontFamily.Cursive
        else -> null
    }
}
