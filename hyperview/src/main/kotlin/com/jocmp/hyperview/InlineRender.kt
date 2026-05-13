package com.jocmp.hyperview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

internal const val URL_ANNOTATION_TAG = "URL"
internal const val LINK_TEXT_ANNOTATION_TAG = "URL_TEXT"

/**
 * Renders a flat list of [HtmlNode.Inline] runs as an [AnnotatedString]. Inline
 * styles map to [SpanStyle]; links are exposed as string annotations under
 * [URL_ANNOTATION_TAG] so the surrounding Text can resolve tap + long-press
 * via [androidx.compose.ui.text.TextLayoutResult.getOffsetForPosition].
 */
internal fun List<HtmlNode.Inline>.toAnnotatedString(style: HtmlStyle): AnnotatedString =
    buildAnnotatedString { appendInlines(this@toAnnotatedString, style) }

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<HtmlNode.Inline>,
    style: HtmlStyle,
) {
    for (inline in inlines) {
        when (inline) {
            is HtmlNode.Inline.Text -> withStyle(inline.style.toSpanStyle(style)) {
                append(inline.text)
            }
            is HtmlNode.Inline.LineBreak -> append('\n')
            is HtmlNode.Inline.Code -> withStyle(
                SpanStyle(
                    fontFamily = style.code.fontFamily,
                    fontSize = style.code.fontSize,
                    background = style.codeBlockBackground,
                )
            ) {
                append(inline.text)
            }
            is HtmlNode.Inline.Link -> {
                val start = length
                withStyle(
                    SpanStyle(
                        color = style.link.color,
                        textDecoration = TextDecoration.Underline,
                    )
                ) {
                    appendInlines(inline.children, style)
                }
                val end = length
                addStringAnnotation(URL_ANNOTATION_TAG, inline.href, start, end)
                addStringAnnotation(
                    LINK_TEXT_ANNOTATION_TAG,
                    inline.children.flattenText(),
                    start,
                    end,
                )
            }
        }
    }
}

private fun List<HtmlNode.Inline>.flattenText(): String = buildString {
    fun visit(nodes: List<HtmlNode.Inline>) {
        for (node in nodes) when (node) {
            is HtmlNode.Inline.Text -> append(node.text)
            is HtmlNode.Inline.Code -> append(node.text)
            is HtmlNode.Inline.LineBreak -> append('\n')
            is HtmlNode.Inline.Link -> visit(node.children)
        }
    }
    visit(this@flattenText)
}

private fun InlineStyle.toSpanStyle(style: HtmlStyle): SpanStyle {
    val decorations = mutableListOf<TextDecoration>()
    if (has(InlineStyle.UNDERLINE)) decorations += TextDecoration.Underline
    if (has(InlineStyle.STRIKETHROUGH)) decorations += TextDecoration.LineThrough

    val baseline = when {
        has(InlineStyle.SUPERSCRIPT) -> BaselineShift.Superscript
        has(InlineStyle.SUBSCRIPT) -> BaselineShift.Subscript
        else -> null
    }

    return SpanStyle(
        fontWeight = if (has(InlineStyle.BOLD)) FontWeight.Bold else null,
        fontStyle = if (has(InlineStyle.ITALIC)) FontStyle.Italic else null,
        textDecoration = decorations.takeIf { it.isNotEmpty() }?.let(TextDecoration::combine),
        baselineShift = baseline,
        fontSize = if (has(InlineStyle.SMALL)) style.body.fontSize * 0.85f else androidx.compose.ui.unit.TextUnit.Unspecified,
        background = if (has(InlineStyle.MARK)) MarkHighlight else Color.Unspecified,
    )
}

private val MarkHighlight = Color(0xFFFFF59D)
