package com.jocmp.mallet.tex

/**
 * The CSS properties KaTeX's builders set inline. Values keep KaTeX's CSS strings, e.g.
 * `makeEm(0.5)`; [com.jocmp.mallet.tex.layout.TexLayout] reads them back.
 */
class CssStyle(
    var backgroundColor: String? = null,
    var borderBottomWidth: String? = null,
    var borderColor: String? = null,
    var borderRightStyle: String? = null,
    var borderRightWidth: String? = null,
    var borderTopWidth: String? = null,
    var borderStyle: String? = null,
    var borderWidth: String? = null,
    var bottom: String? = null,
    var color: String? = null,
    var height: String? = null,
    var left: String? = null,
    var margin: String? = null,
    var marginLeft: String? = null,
    var marginRight: String? = null,
    var marginTop: String? = null,
    var minWidth: String? = null,
    var paddingLeft: String? = null,
    var position: String? = null,
    var textShadow: String? = null,
    var top: String? = null,
    var width: String? = null,
    var verticalAlign: String? = null,
) {
    fun copy(): CssStyle =
        CssStyle(
            backgroundColor, borderBottomWidth, borderColor, borderRightStyle, borderRightWidth, borderTopWidth,
            borderStyle, borderWidth, bottom, color, height, left, margin, marginLeft, marginRight, marginTop,
            minWidth, paddingLeft, position, textShadow, top, width, verticalAlign,
        )

    private fun values(): List<String?> =
        listOf(
            backgroundColor, borderBottomWidth, borderColor, borderRightStyle, borderRightWidth, borderTopWidth,
            borderStyle, borderWidth, bottom, color, height, left, margin, marginLeft, marginRight, marginTop,
            minWidth, paddingLeft, position, textShadow, top, width, verticalAlign,
        )

    override fun equals(other: Any?): Boolean = other is CssStyle && values() == other.values()

    override fun hashCode(): Int = values().hashCode()
}

sealed interface VirtualNode

/** A node in KaTeX's HTML output tree, with TeX dimensions in ems. */
sealed interface HtmlDomNode : VirtualNode {
    val classes: MutableList<String>
    var height: Double
    var depth: Double
    var maxFontSize: Double
    val style: CssStyle

    fun hasClass(className: String): Boolean = classes.contains(className)
}

private fun initNode(classes: MutableList<String>, style: CssStyle, options: Options?) {
    if (options == null) {
        return
    }
    if (options.style.isTight()) {
        classes.add("mtight")
    }
    options.getColor()?.let { style.color = it }
}

open class Span(
    classes: List<String> = emptyList(),
    children: List<VirtualNode> = emptyList(),
    options: Options? = null,
    style: CssStyle? = null,
) : HtmlDomNode {
    override val classes: MutableList<String> = classes.toMutableList()
    val children: MutableList<VirtualNode> = children.toMutableList()
    override val style: CssStyle = style ?: CssStyle()
    val attributes = mutableMapOf<String, String>()
    override var height = 0.0
    override var depth = 0.0
    override var maxFontSize = 0.0
    var width: Double? = null
    var italic: Double? = null

    /** Set on `\middle` delimiters so `\left`...`\right` can resize them. */
    var isMiddle: MiddleDelim? = null

    init {
        initNode(this.classes, this.style, options)
    }

    fun setAttribute(attribute: String, value: String) {
        attributes[attribute] = value
    }
}

class MiddleDelim(
    val delim: String,
    val options: Options,
)

class Anchor(
    val href: String,
    classes: List<String>,
    children: List<HtmlDomNode>,
    options: Options,
) : Span(classes, children, options)

class Img(
    val src: String,
    val alt: String,
    override val style: CssStyle,
) : HtmlDomNode {
    override val classes = mutableListOf("mord")
    override var height = 0.0
    override var depth = 0.0
    override var maxFontSize = 0.0
}

private val iCombinations =
    mapOf(
        "î" to "ı̂",
        "ï" to "ı̈",
        "í" to "ı́",
        "ì" to "ı̀",
    )

/**
 * A single glyph run. Unlike KaTeX, it records [fontName] and whether KaTeX had metrics for it,
 * since there is no browser to resolve fonts or measure fallback text.
 */
class SymbolNode(
    text: String,
    override var height: Double = 0.0,
    override var depth: Double = 0.0,
    var italic: Double = 0.0,
    var skew: Double = 0.0,
    var width: Double = 0.0,
    classes: List<String> = emptyList(),
    style: CssStyle? = null,
) : HtmlDomNode {
    var text: String = iCombinations[text] ?: text
    override val classes: MutableList<String> = classes.toMutableList()
    override val style: CssStyle = style ?: CssStyle()
    override var maxFontSize = 0.0
    var fontName: String? = null
    var hasMetrics: Boolean = false

    init {
        text.firstOrNull()?.let { first ->
            scriptFromCodepoint(first.code)?.let { this.classes.add("${it}_fallback") }
        }
    }
}

sealed interface SvgChildNode

class PathNode(
    val pathName: String,
    val alternate: String? = null,
) : SvgChildNode

class LineNode(
    val attributes: Map<String, String> = emptyMap(),
) : SvgChildNode

class SvgNode(
    val children: List<SvgChildNode> = emptyList(),
    val attributes: MutableMap<String, String> = mutableMapOf(),
) : VirtualNode

class DocumentFragment(
    children: List<HtmlDomNode>,
) : HtmlDomNode {
    val children: MutableList<HtmlDomNode> = children.toMutableList()
    override val classes = mutableListOf<String>()
    override var height = 0.0
    override var depth = 0.0
    override var maxFontSize = 0.0
    override val style = CssStyle()
}

/**
 * One child of a [VListSpan]: [elem] sits with its baseline [baseline] ems below the vlist's
 * baseline (negative is above), inside a wrapper carrying KaTeX's wrapper classes and style.
 */
class VListEntry(
    val elem: HtmlDomNode,
    val wrapper: Span,
    val baseline: Double,
)

/**
 * KaTeX's `vlist-t` table. KaTeX positions rows with pstrut spans and relative CSS offsets;
 * this keeps the resolved offsets instead.
 */
class VListSpan(
    val entries: List<VListEntry>,
) : Span(listOf("vlist-t"), entries.map { it.wrapper })
