package com.jocmp.mallet.tex

import java.util.IdentityHashMap

/** Laid-out math, in ems of the base font size. y grows downward from the baseline. */
class TexBox(
    val width: Double,
    val height: Double,
    val depth: Double,
    val items: List<TexItem>,
)

sealed interface TexItem {
    /** [text] drawn in [fontName] at [size] ems with its baseline origin at ([x], [y]). */
    data class Glyph(
        val x: Double,
        val y: Double,
        val text: String,
        val fontName: String,
        val size: Double,
        val color: String?,
        val mirrored: Boolean = false,
    ) : TexItem

    data class Rect(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double,
        val color: String?,
        val dashed: Boolean = false,
    ) : TexItem

    /** An SVG viewport at ([x], [y], [width], [height]), clipped to [clip] when set. */
    data class Svg(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double,
        val viewBox: List<Double>?,
        val preserveAspectRatio: String?,
        val paths: List<String>,
        val clip: Clip?,
        val color: String?,
    ) : TexItem

    data class Line(
        val x1: Double,
        val y1: Double,
        val x2: Double,
        val y2: Double,
        val strokeWidth: Double,
        val color: String?,
    ) : TexItem

    data class Clip(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double,
    )
}

/** Measures text KaTeX has no metrics for, which a browser would measure in a fallback font. */
fun interface TexTextMeasurer {
    fun measure(text: String, fontName: String): Double
}

private val defaultMeasurer = TexTextMeasurer { text, _ -> 0.6 * text.codePointCount(0, text.length) }

private val sizingFactors = sizeMultipliers

/**
 * Lays out KaTeX's HTML tree by emulating the parts of katex.css its builders rely on: inline
 * runs of inline-blocks on a shared baseline, vlists, sizing, margins, padding, borders, laps,
 * relative offsets, and absolutely positioned SVG.
 */
class TexLayout(
    private val measurer: TexTextMeasurer = defaultMeasurer,
    displayMode: Boolean = false,
) {
    private val naturalWidths = IdentityHashMap<VirtualNode, Double>()
    private val equationNumbers = IdentityHashMap<Span, Int>()
    private val rootAlign = if (displayMode) Align.CENTER else Align.LEFT

    fun layout(root: Span): Pair<TexBox, TexBox?> {
        val context = Context(color = null, align = rootAlign)
        val tag = root.children.filterIsInstance<Span>().find { it.hasClass("katex-tag") }
        val body = root.children.filter { it !== tag }
        val laid = layoutRun(body, context, fill = null)
        val box = TexBox(laid.width, root.height, root.depth, laid.items)
        val tagBox =
            tag?.let {
                val tagLaid = layoutRun(it.children, context, fill = null)
                TexBox(tagLaid.width, it.height, it.depth, tagLaid.items)
            }
        return box to tagBox
    }

    private enum class Align { LEFT, CENTER, RIGHT }

    private data class Context(
        val color: String?,
        val align: Align,
        val clip: TexItem.Clip? = null,
    )

    private class Laid(
        val width: Double,
        val items: List<TexItem>,
    )

    private class Edges(
        val left: Double = 0.0,
        val right: Double = 0.0,
        val top: Double = 0.0,
        val bottom: Double = 0.0,
    )

    private fun layoutNode(node: VirtualNode, context: Context, fill: Double?): Laid =
        when (node) {
            is SymbolNode -> layoutSymbol(node, context)
            is DocumentFragment -> layoutRun(node.children, context, fill)
            is VListSpan -> layoutVList(node, context)
            is Span -> layoutSpan(node, context, fill)
            is Img -> Laid(0.0, emptyList())
            is SvgNode -> Laid(0.0, emptyList())
        }

    private fun naturalWidth(node: VirtualNode, context: Context): Double =
        naturalWidths.getOrPut(node) { layoutNode(node, context, fill = null).width }

    /** A glyph run; like KaTeX's `SymbolNode.toNode()`, its inline style applies to the glyph's span. */
    private fun layoutSymbol(node: SymbolNode, context: Context): Laid {
        val fontName = node.fontName ?: "Main-Regular"
        val width = if (node.hasMetrics) node.width else measurer.measure(node.text, fontName)
        val style = node.style
        val color = style.color ?: context.color
        val paddingLeft = em(style.paddingLeft) ?: 0.0
        val dx = paddingLeft + (em(style.left) ?: 0.0)
        val dy = (em(style.top) ?: 0.0) - (em(style.bottom) ?: 0.0) - (em(style.verticalAlign) ?: 0.0)
        val items =
            if (node.text.isEmpty() || node.text == "​" || color == "transparent") {
                emptyList()
            } else {
                listOf(TexItem.Glyph(dx, dy, node.text, fontName, 1.0, color))
            }
        return Laid(paddingLeft + width, items)
    }

    /** Children placed left to right on a shared baseline, as inline-blocks. */
    private fun layoutRun(children: List<VirtualNode>, context: Context, fill: Double?): Laid {
        var x = 0.0
        val items = mutableListOf<TexItem>()
        children.forEach { child ->
            if (child is SvgNode || isAbsolute(child)) {
                return@forEach
            }
            val (marginLeft, marginRight) = margins(child)
            val laid = layoutNode(child, context, fill)
            items.addAll(translate(laid.items, x + marginLeft, 0.0))
            val italic = (child as? SymbolNode)?.italic?.takeIf { it > 0 } ?: 0.0
            x += marginLeft + laid.width + marginRight + italic
        }
        return Laid(x, items)
    }

    private fun layoutVList(vlist: VListSpan, context: Context): Laid {
        val align = alignFor(vlist, context.align)
        val entryContexts =
            vlist.entries.map { entry ->
                context.copy(
                    color = entry.wrapper.style.color ?: context.color,
                    align = alignFor(entry.wrapper, align),
                )
            }
        val width =
            vlist.entries.indices.maxOfOrNull { i ->
                val entry = vlist.entries[i]
                val (marginLeft, marginRight) = margins(entry.wrapper)
                marginLeft + naturalWidth(entry.elem, entryContexts[i]) + marginRight
            } ?: 0.0

        val items = mutableListOf<TexItem>()
        vlist.entries.forEachIndexed { i, entry ->
            val (marginLeft, marginRight) = margins(entry.wrapper)
            val laid = layoutNode(entry.elem, entryContexts[i], width - marginLeft - marginRight)
            val entryWidth = marginLeft + laid.width + marginRight
            val offset =
                when (entryContexts[i].align) {
                    Align.LEFT -> 0.0
                    Align.CENTER -> (width - entryWidth) / 2
                    Align.RIGHT -> width - entryWidth
                }
            items.addAll(translate(laid.items, offset + marginLeft, entry.baseline))
        }
        return Laid(width, items)
    }

    private fun layoutSpan(span: Span, parentContext: Context, parentFill: Double?): Laid {
        val factor = sizingFactor(span)
        val fill = parentFill?.div(factor)
        val style = span.style
        val color = style.color ?: parentContext.color
        val clipsChildren = span.classes.any { it in overflowHidden }
        var context = parentContext.copy(color = color, align = alignFor(span, parentContext.align))

        val border = borders(span)
        val padding = paddings(span)
        val horizontalExtras = border.left + padding.left + padding.right + border.right

        val explicitWidth = explicitWidth(span, fill, horizontalExtras)
        val innerFill = explicitWidth ?: if (style.width == null && isBlock(span)) fill?.minus(horizontalExtras) else null

        val top = boxTop(span)
        val bottom = boxBottom(span)
        if (clipsChildren) {
            val clipWidth = (explicitWidth ?: innerFill ?: 0.0) + horizontalExtras
            context = context.copy(clip = TexItem.Clip(0.0, top, clipWidth, bottom - top))
        }

        val run = if (span.hasClass("eqn-num")) equationNumber(span, context) else layoutRun(span.children, context, innerFill)
        var contentWidth = explicitWidth ?: run.width
        em(style.minWidth)?.let { contentWidth = maxOf(contentWidth, it - horizontalExtras) }
        if (span.classes.any { it in zeroWidthClasses } && !span.hasClass("accent-full")) {
            contentWidth = 0.0
        }
        if (span.hasClass("nulldelimiter")) {
            contentWidth = 0.12
        }
        val width = contentWidth + horizontalExtras

        val items = mutableListOf<TexItem>()
        style.backgroundColor?.let { background ->
            items.add(TexItem.Rect(0.0, top, width, bottom - top, background))
        }
        items.addAll(borderRects(span, border, width, top, bottom, color))
        items.addAll(translate(run.items, border.left + padding.left, 0.0))
        val svgWidth = (explicitWidth ?: fill?.minus(horizontalExtras) ?: contentWidth) + horizontalExtras
        items.addAll(absoluteChildren(span, context, width, svgWidth, top, bottom))

        if (style.textShadow != null) {
            items.addAll(items.filterIsInstance<TexItem.Glyph>().map { it.copy(x = it.x + 0.02, y = it.y + 0.01) })
        }
        var result = if (span.hasClass("reflectbox")) mirror(items, width) else items

        var dx = em(style.left) ?: 0.0
        var dy = (em(style.top) ?: 0.0) - (em(style.bottom) ?: 0.0) - (em(style.verticalAlign) ?: 0.0)
        if (factor != 1.0) {
            result = scale(result, factor)
            dx *= factor
            dy *= factor
        }
        return Laid(width * factor, translate(result, dx, dy))
    }

    /**
     * An SVG with no width of its own fills its nearest positioned ancestor, which for KaTeX is
     * usually the vlist row, hence [svgWidth] rather than the span's own [width].
     */
    /** KaTeX numbers equations with a CSS counter, `.eqn-num::before { content: "(" counter ")" }`. */
    private fun equationNumber(span: Span, context: Context): Laid {
        val number = equationNumbers.getOrPut(span) { equationNumbers.size + 1 }
        var x = 0.0
        val items = mutableListOf<TexItem>()
        "($number)".forEach { char ->
            val text = char.toString()
            if (context.color != "transparent") {
                items.add(TexItem.Glyph(x, 0.0, text, "Main-Regular", 1.0, context.color))
            }
            x += getCharacterMetrics(text, "Main-Regular", Mode.TEXT)?.width ?: measurer.measure(text, "Main-Regular")
        }
        return Laid(x, items)
    }

    private fun absoluteChildren(span: Span, context: Context, width: Double, svgWidth: Double, top: Double, bottom: Double): List<TexItem> {
        val items = mutableListOf<TexItem>()
        span.children.forEach { child ->
            when {
                child is SvgNode -> items.addAll(layoutSvg(child, span, context, svgWidth, top))
                child is Span && child.hasClass("katex-inner") -> {
                    val laid = layoutSpan(child, context, null)
                    val x =
                        when {
                            span.hasClass("llap") -> -laid.width
                            span.hasClass("clap") -> -laid.width / 2
                            else -> 0.0
                        }
                    items.addAll(translate(laid.items, x, 0.0))
                }
                child is Span && child.classes.any { it in percentPieces } -> {
                    val (left, pieceWidth) = percentPiece(child, width)
                    val pieceContext = context.copy(clip = TexItem.Clip(left, top, pieceWidth, bottom - top))
                    val laid = layoutSpan(child, pieceContext, pieceWidth)
                    items.addAll(translate(laid.items, left, 0.0))
                }
            }
        }
        return items
    }

    private fun layoutSvg(svg: SvgNode, parent: Span, context: Context, parentWidth: Double, parentTop: Double): List<TexItem> {
        val styleWidth = svg.attributes["style"]?.substringAfter("width:", "")?.let { em(it) }
        val width = styleWidth ?: parentWidth
        val height = em(parent.style.height) ?: em(svg.attributes["height"]) ?: 0.0
        val top = if (parent.style.height != null) parentTop else -height
        val color = context.color
        if (color == "transparent") {
            return emptyList()
        }
        val items = mutableListOf<TexItem>()
        val paths = svg.children.filterIsInstance<PathNode>().mapNotNull { it.alternate ?: SvgGeometry.path[it.pathName] }
        if (paths.isNotEmpty()) {
            val viewBox = svg.attributes["viewBox"]?.trim()?.split(Regex("[\\s,]+"))?.map { it.toDouble() }
            items.add(
                TexItem.Svg(
                    x = 0.0,
                    y = top,
                    width = width,
                    height = height,
                    viewBox = viewBox,
                    preserveAspectRatio = svg.attributes["preserveAspectRatio"],
                    paths = paths,
                    clip = context.clip,
                    color = color,
                ),
            )
        }
        svg.children.filterIsInstance<LineNode>().forEach { line ->
            fun coordinate(value: String?, extent: Double): Double =
                when {
                    value == null -> 0.0
                    value.endsWith("%") -> value.dropLast(1).toDouble() / 100 * extent
                    else -> em(value) ?: value.toDoubleOrNull()?.div(1000) ?: 0.0
                }
            items.add(
                TexItem.Line(
                    x1 = coordinate(line.attributes["x1"], width),
                    y1 = top + coordinate(line.attributes["y1"], height),
                    x2 = coordinate(line.attributes["x2"], width),
                    y2 = top + coordinate(line.attributes["y2"], height),
                    strokeWidth = em(line.attributes["stroke-width"]) ?: 0.04,
                    color = color,
                ),
            )
        }
        return items
    }

    private fun percentPiece(piece: Span, parentWidth: Double): Pair<Double, Double> =
        when {
            piece.hasClass("halfarrow-left") -> 0.0 to parentWidth * 0.502
            piece.hasClass("halfarrow-right") -> parentWidth * (1 - 0.502) to parentWidth * 0.502
            piece.hasClass("brace-left") -> 0.0 to parentWidth * 0.251
            piece.hasClass("brace-center") -> parentWidth * 0.25 to parentWidth * 0.5
            else -> parentWidth * (1 - 0.251) to parentWidth * 0.251
        }

    private fun isAbsolute(node: VirtualNode): Boolean =
        node is Span && (node.hasClass("katex-inner") || node.classes.any { it in percentPieces })

    private fun isBlock(span: Span): Boolean = span.classes.any { it in blockClasses }

    private fun explicitWidth(span: Span, fill: Double?, horizontalExtras: Double): Double? {
        val width = span.style.width
        if (width == null) {
            if (span.classes.any { it in fullWidthClasses }) {
                return fill?.minus(horizontalExtras) ?: 0.0
            }
            return null
        }
        if (width.endsWith("%")) {
            return (fill ?: 0.0) * width.dropLast(1).toDouble() / 100 - horizontalExtras
        }
        val value = em(width) ?: return null
        return if (span.classes.any { it in borderBoxClasses }) value - horizontalExtras else value
    }

    private fun boxTop(span: Span): Double = em(span.style.height)?.let { -it } ?: -span.height

    private fun boxBottom(span: Span): Double = if (span.style.height != null) 0.0 else span.depth

    private fun sizingFactor(span: Span): Double {
        if (!span.hasClass("katex-sizing") && !span.hasClass("fontsize-ensurer")) {
            return 1.0
        }
        val from = span.classes.firstNotNullOfOrNull { it.removePrefix("reset-size").takeIf { _ -> it.startsWith("reset-size") }?.toIntOrNull() }
        val to = span.classes.firstNotNullOfOrNull { cls -> if (cls.startsWith("size")) cls.removePrefix("size").toIntOrNull() else null }
        if (from == null || to == null) {
            return 1.0
        }
        return sizingFactors[to - 1] / sizingFactors[from - 1]
    }

    private fun alignFor(node: Span, inherited: Align): Align =
        when {
            node.hasClass("msupsub") || node.hasClass("svg-align") || node.hasClass("col-align-l") -> Align.LEFT
            node.hasClass("col-align-r") -> Align.RIGHT
            node.classes.any { it in centeringClasses } -> Align.CENTER
            else -> inherited
        }

    private fun margins(node: VirtualNode): Pair<Double, Double> {
        if (node is SymbolNode) {
            return (em(node.style.marginLeft) ?: 0.0) to (em(node.style.marginRight) ?: 0.0)
        }
        if (node !is Span) {
            return 0.0 to 0.0
        }
        var left = node.classes.sumOf { classMarginLeft[it] ?: 0.0 }
        var right = node.classes.sumOf { classMarginRight[it] ?: 0.0 }
        node.style.margin?.let { margin ->
            val parts = margin.trim().split(Regex("\\s+")).map { em(it) ?: 0.0 }
            when (parts.size) {
                1 -> {
                    left = parts[0]
                    right = parts[0]
                }
                2, 3 -> {
                    left = parts[1]
                    right = parts[1]
                }
                4 -> {
                    right = parts[1]
                    left = parts[3]
                }
            }
        }
        em(node.style.marginLeft)?.let { left = it }
        em(node.style.marginRight)?.let { right = it }
        val factor = sizingFactor(node)
        return left * factor to right * factor
    }

    private fun paddings(span: Span): Edges {
        var left = span.classes.sumOf { classPaddingLeft[it] ?: 0.0 }
        val right = span.classes.sumOf { classPaddingRight[it] ?: 0.0 }
        em(span.style.paddingLeft)?.let { left = it }
        return Edges(left = left, right = right)
    }

    private fun borders(span: Span): Edges {
        val style = span.style
        val all = em(style.borderWidth)
        return when {
            span.hasClass("fbox") || span.hasClass("fcolorbox") -> {
                val width = all ?: 0.04
                Edges(width, width, width, width)
            }
            span.hasClass("angl") -> Edges(right = all ?: 0.049, top = all ?: 0.049)
            span.hasClass("katex-sout") -> Edges(bottom = em(style.borderBottomWidth) ?: 0.08)
            span.classes.any { it in bottomRuleClasses } -> Edges(bottom = em(style.borderBottomWidth) ?: 0.0)
            else ->
                Edges(
                    left = all ?: 0.0,
                    right = em(style.borderRightWidth) ?: all ?: 0.0,
                    top = em(style.borderTopWidth) ?: all ?: 0.0,
                    bottom = em(style.borderBottomWidth) ?: all ?: 0.0,
                )
        }
    }

    private fun borderRects(span: Span, border: Edges, width: Double, top: Double, bottom: Double, color: String?): List<TexItem> {
        val borderColor = span.style.borderColor ?: color
        if (borderColor == "transparent") {
            return emptyList()
        }
        val dashed = span.hasClass("katex-hdashline") || span.style.borderRightStyle == "dashed" || span.style.borderStyle == "dashed"
        val hasContent = span.children.isNotEmpty()
        val boxTop = if (!hasContent && span.style.height == null) -(border.top + border.bottom) else top
        val boxBottom = if (!hasContent && span.style.height == null) 0.0 else bottom
        val rects = mutableListOf<TexItem>()
        if (border.top > 0) {
            rects.add(TexItem.Rect(0.0, boxTop, width, border.top, borderColor, dashed))
        }
        if (border.bottom > 0) {
            rects.add(TexItem.Rect(0.0, boxBottom - border.bottom, width, border.bottom, borderColor, dashed))
        }
        if (border.left > 0) {
            rects.add(TexItem.Rect(0.0, boxTop, border.left, boxBottom - boxTop, borderColor, dashed))
        }
        if (border.right > 0) {
            rects.add(TexItem.Rect(width - border.right, boxTop, border.right, boxBottom - boxTop, borderColor, dashed))
        }
        return rects
    }

    private fun translate(items: List<TexItem>, dx: Double, dy: Double): List<TexItem> {
        if (dx == 0.0 && dy == 0.0) {
            return items
        }
        return items.map { item ->
            when (item) {
                is TexItem.Glyph -> item.copy(x = item.x + dx, y = item.y + dy)
                is TexItem.Rect -> item.copy(x = item.x + dx, y = item.y + dy)
                is TexItem.Svg -> item.copy(x = item.x + dx, y = item.y + dy, clip = item.clip?.let { it.copy(x = it.x + dx, y = it.y + dy) })
                is TexItem.Line -> item.copy(x1 = item.x1 + dx, y1 = item.y1 + dy, x2 = item.x2 + dx, y2 = item.y2 + dy)
            }
        }
    }

    private fun scale(items: List<TexItem>, factor: Double): List<TexItem> =
        items.map { item ->
            when (item) {
                is TexItem.Glyph -> item.copy(x = item.x * factor, y = item.y * factor, size = item.size * factor)
                is TexItem.Rect -> item.copy(x = item.x * factor, y = item.y * factor, width = item.width * factor, height = item.height * factor)
                is TexItem.Svg ->
                    item.copy(
                        x = item.x * factor,
                        y = item.y * factor,
                        width = item.width * factor,
                        height = item.height * factor,
                        clip = item.clip?.let { TexItem.Clip(it.x * factor, it.y * factor, it.width * factor, it.height * factor) },
                    )
                is TexItem.Line ->
                    item.copy(
                        x1 = item.x1 * factor,
                        y1 = item.y1 * factor,
                        x2 = item.x2 * factor,
                        y2 = item.y2 * factor,
                        strokeWidth = item.strokeWidth * factor,
                    )
            }
        }

    private fun mirror(items: List<TexItem>, width: Double): List<TexItem> =
        items.map { item ->
            when (item) {
                is TexItem.Glyph -> item.copy(x = width - item.x, mirrored = !item.mirrored)
                is TexItem.Rect -> item.copy(x = width - item.x - item.width)
                is TexItem.Svg -> item
                is TexItem.Line -> item.copy(x1 = width - item.x1, x2 = width - item.x2)
            }
        }

    private companion object {
        val emRegex = Regex("^\\s*(-?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?)em\\s*$")

        fun em(value: String?): Double? {
            if (value == null) {
                return null
            }
            if (value.trim() == "0") {
                return 0.0
            }
            return emRegex.find(value)?.groupValues?.get(1)?.toDouble()
        }

        val centeringClasses = setOf("mfrac", "op-limits", "katex-accent", "accent", "col-align-c", "x-arrow", "mover", "munder")
        val overflowHidden = setOf("hide-tail", "katex-stretchy", "halfarrow-left", "halfarrow-right", "brace-left", "brace-center", "brace-right")
        val percentPieces = setOf("halfarrow-left", "halfarrow-right", "brace-left", "brace-center", "brace-right")
        val blockClasses = setOf("katex-stretchy", "katex-overlay")
        val fullWidthClasses =
            setOf("frac-line", "overline-line", "underline-line", "katex-hline", "katex-hdashline", "katex-stretchy", "hide-tail") +
                percentPieces
        val borderBoxClasses = setOf("fbox", "fcolorbox", "angl")
        val zeroWidthClasses = setOf("llap", "rlap", "clap", "katex-thinbox", "accent-body")
        val bottomRuleClasses = setOf("frac-line", "overline-line", "underline-line", "katex-hline", "katex-hdashline")

        const val MU = 1.0 / 18

        val classMarginLeft = mapOf("katex-root" to 5 * MU, "cancel-lap" to -0.2)
        val classMarginRight = mapOf("katex-root" to -10 * MU, "cancel-lap" to -0.2, "angl" to 0.03889)
        val classPaddingLeft =
            mapOf("boxpad" to 0.3, "cancel-pad" to 0.2, "x-arrow-pad" to 0.5, "cd-arrow-pad" to 0.27778, "anglpad" to 0.03889)
        val classPaddingRight =
            mapOf("boxpad" to 0.3, "cancel-pad" to 0.2, "x-arrow-pad" to 0.5, "cd-arrow-pad" to 0.55556, "anglpad" to 0.03889)
    }
}
