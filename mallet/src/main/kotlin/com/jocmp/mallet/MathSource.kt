package com.jocmp.mallet

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/**
 * TeX source recovered from server-rendered math markup.
 */
internal data class MathSource(
    val tex: String,
    val display: Boolean,
)

/**
 * Replaces rendered math under this element with its TeX source, so plain-text extraction reads
 * `F_0` instead of KaTeX's MathML and visual copies run together.
 */
fun Element.replaceMathWithSource() {
    selectFirst(MATH_SELECTOR) ?: return
    val root = root()

    select("*").forEach { element ->
        // Descendants of a formula replaced earlier are already detached.
        if (element.root() !== root) {
            return@forEach
        }
        val math = mathSource(element) ?: return@forEach
        element.replaceWith(TextNode(math.tex))
    }
}

/**
 * Finds the TeX behind [element] when it is the root of a rendered formula, or null.
 *
 * KaTeX and MathML output keep the source in a `<annotation encoding="application/x-tex">`.
 * Pandoc leaves delimited source in `span.math`, and MathJax 2 in `script[type=math/tex]`.
 */
internal fun mathSource(element: Element): MathSource? {
    val tag = element.tagName()

    if (tag == "span" || tag == "div") {
        if (element.hasClass("katex-display")) {
            return element.texAnnotation()?.let { MathSource(it, display = true) }
        }
        if (element.hasClass("katex")) {
            return element.texAnnotation()?.let { MathSource(it, display = false) }
        }
        if (element.hasClass("math") && (element.hasClass("inline") || element.hasClass("display"))) {
            return pandocMath(element)
        }
    }

    if (tag == "math") {
        val display = element.attr("display") == "block"
        return element.texAnnotation()?.let { MathSource(it, display) }
    }

    if (tag == "script") {
        val type = element.attr("type")
        if (type.startsWith("math/tex")) {
            val tex = element.data().trim()
            return tex.takeIf { it.isNotEmpty() }?.let { MathSource(it, display = type.contains("mode=display")) }
        }
    }

    return null
}

private fun Element.texAnnotation(): String? =
    selectFirst("annotation[encoding=application/x-tex]")
        ?.wholeText()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

private fun pandocMath(element: Element): MathSource? {
    val display = element.hasClass("display")

    // Static site generators can wrap server-rendered KaTeX in Pandoc's span.
    element.texAnnotation()?.let { return MathSource(it, display) }

    val text = element.wholeText().trim()
    val tex =
        delimiters.firstNotNullOfOrNull { (open, close) ->
            if (text.startsWith(open) && text.endsWith(close) && text.length >= open.length + close.length) {
                text.substring(open.length, text.length - close.length)
            } else {
                null
            }
        } ?: text

    return tex.trim().takeIf { it.isNotEmpty() }?.let { MathSource(it, display) }
}

private const val MATH_SELECTOR = ".katex, .katex-display, span.math, math, script[type^=math/tex]"

private val delimiters =
    listOf(
        "\\(" to "\\)",
        "\\[" to "\\]",
        "$$" to "$$",
        "$" to "$",
    )

private val whitespace = Regex("\\s+")

/**
 * TeX on one line, for showing the source inline.
 */
internal fun String.collapseWhitespace(): String = replace(whitespace, " ")
