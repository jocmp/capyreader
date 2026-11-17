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

    /**
     * Every node lays out into this one buffer and is then shifted in place, so items are not
     * copied once per nesting level.
     */
    private val out = ArrayList<Item>()
    private val prefix = ArrayList<Item>()
    private val margin = Margin()

    /** Above zero inside a natural-width pass, which needs widths only, so no items are made. */
    private var measuring = 0

    fun layout(root: Span): Pair<TexBox, TexBox?> {
        val context = Context(color = null, align = rootAlign)
        val tag = root.children.filterIsInstance<Span>().find { it.has(KATEX_TAG) }
        val body = root.children.filter { it !== tag }
        val width = layoutRun(body, context, fill = null)
        val box = TexBox(width, root.height, root.depth, publish(0))
        val tagBox =
            tag?.let {
                val tagWidth = layoutRun(it.children, context, fill = null)
                TexBox(tagWidth, it.height, it.depth, publish(0))
            }
        return box to tagBox
    }

    private enum class Align { LEFT, CENTER, RIGHT }

    private enum class Kind { GLYPH, RECT, SVG, LINE }

    private data class Context(
        val color: String?,
        val align: Align,
        val clip: TexItem.Clip? = null,
    )

    private class Margin(
        var left: Double = 0.0,
        var right: Double = 0.0,
    )

    private class Edges(
        val left: Double = 0.0,
        val right: Double = 0.0,
        val top: Double = 0.0,
        val bottom: Double = 0.0,
    )

    /**
     * A mutable [TexItem] while layout shifts it into place. For a line, ([x], [y]) is its first
     * end and ([x2], [y2]) its second. Each SVG keeps its own copy of the clip it was drawn with.
     */
    private class Item(
        val kind: Kind,
        var x: Double,
        var y: Double,
        var width: Double = 0.0,
        var height: Double = 0.0,
        var x2: Double = 0.0,
        var y2: Double = 0.0,
        var size: Double = 1.0,
        var strokeWidth: Double = 0.0,
        val text: String = "",
        val fontName: String = "",
        val color: String? = null,
        var mirrored: Boolean = false,
        val dashed: Boolean = false,
        val viewBox: List<Double>? = null,
        val preserveAspectRatio: String? = null,
        val paths: List<String> = emptyList(),
        var clip: DoubleArray? = null,
    ) {
        fun toTexItem(): TexItem =
            when (kind) {
                Kind.GLYPH -> TexItem.Glyph(x, y, text, fontName, size, color, mirrored)
                Kind.RECT -> TexItem.Rect(x, y, width, height, color, dashed)
                Kind.SVG ->
                    TexItem.Svg(
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                        viewBox = viewBox,
                        preserveAspectRatio = preserveAspectRatio,
                        paths = paths,
                        clip = clip?.let { TexItem.Clip(it[0], it[1], it[2], it[3]) },
                        color = color,
                    )
                Kind.LINE -> TexItem.Line(x, y, x2, y2, strokeWidth, color)
            }
    }

    /** The items from [start] on, as the public immutable type; the buffer drops them. */
    private fun publish(start: Int): List<TexItem> {
        val items = ArrayList<TexItem>(out.size - start)
        (start until out.size).forEach { i -> items.add(out[i].toTexItem()) }
        truncate(start)
        return items
    }

    private fun truncate(start: Int) {
        out.subList(start, out.size).clear()
    }

    private fun layoutNode(node: VirtualNode, context: Context, fill: Double?): Double =
        when (node) {
            is SymbolNode -> layoutSymbol(node, context)
            is DocumentFragment -> layoutRun(node.children, context, fill)
            is VListSpan -> layoutVList(node, context)
            is Span -> layoutSpan(node, context, fill)
            is Img -> 0.0
            is SvgNode -> 0.0
        }

    private fun naturalWidth(node: VirtualNode, context: Context): Double =
        naturalWidths.getOrPut(node) {
            measuring += 1
            try {
                layoutNode(node, context, fill = null)
            } finally {
                measuring -= 1
            }
        }

    /** A glyph run; like KaTeX's `SymbolNode.toNode()`, its inline style applies to the glyph's span. */
    private fun layoutSymbol(node: SymbolNode, context: Context): Double {
        val fontName = node.fontName ?: "Main-Regular"
        val width = advanceOverride(fontName, node.text) ?: if (node.hasMetrics) node.width else measurer.measure(node.text, fontName)
        val style = node.style
        val color = style.color ?: context.color
        val paddingLeft = em(style.paddingLeft) ?: 0.0
        val dx = paddingLeft + (em(style.left) ?: 0.0)
        val dy = (em(style.top) ?: 0.0) - (em(style.bottom) ?: 0.0) - (em(style.verticalAlign) ?: 0.0)
        if (measuring == 0 && node.text.isNotEmpty() && node.text != "​" && color != "transparent") {
            out.add(Item(Kind.GLYPH, dx, dy, text = node.text, fontName = fontName, color = color))
        }
        return paddingLeft + width
    }

    /** Children placed left to right on a shared baseline, as inline-blocks. */
    private fun layoutRun(children: List<VirtualNode>, context: Context, fill: Double?): Double {
        var x = 0.0
        children.forEach { child ->
            if (child is SvgNode || isAbsolute(child)) {
                return@forEach
            }
            margins(child)
            val marginLeft = margin.left
            val marginRight = margin.right
            val start = out.size
            val width = layoutNode(child, context, fill)
            shift(start, x + marginLeft, 0.0)
            val italic = (child as? SymbolNode)?.italic?.takeIf { it > 0 } ?: 0.0
            x += marginLeft + width + marginRight + italic
        }
        return x
    }

    private fun layoutVList(vlist: VListSpan, context: Context): Double {
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
                margins(entry.wrapper)
                val marginLeft = margin.left
                val marginRight = margin.right
                marginLeft + naturalWidth(entry.elem, entryContexts[i]) + marginRight
            } ?: 0.0

        vlist.entries.forEachIndexed { i, entry ->
            margins(entry.wrapper)
            val marginLeft = margin.left
            val marginRight = margin.right
            val start = out.size
            val laidWidth = layoutNode(entry.elem, entryContexts[i], width - marginLeft - marginRight)
            val entryWidth = marginLeft + laidWidth + marginRight
            val offset =
                when (entryContexts[i].align) {
                    Align.LEFT -> 0.0
                    Align.CENTER -> (width - entryWidth) / 2
                    Align.RIGHT -> width - entryWidth
                }
            shift(start, offset + marginLeft, entry.baseline)
        }
        return width
    }

    private fun layoutSpan(span: Span, parentContext: Context, parentFill: Double?): Double {
        val factor = sizingFactor(span)
        val fill = parentFill?.div(factor)
        val style = span.style
        val color = style.color ?: parentContext.color
        val clipsChildren = span.has(OVERFLOW_HIDDEN)
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

        val start = out.size
        val runWidth = if (span.has(EQN_NUM)) equationNumber(span, context) else layoutRun(span.children, context, innerFill)
        var contentWidth = explicitWidth ?: runWidth
        em(style.minWidth)?.let { contentWidth = maxOf(contentWidth, it - horizontalExtras) }
        if (span.has(ZERO_WIDTH) && !span.has(ACCENT_FULL)) {
            contentWidth = 0.0
        }
        if (span.has(NULLDELIMITER)) {
            contentWidth = 0.12
        }
        val width = contentWidth + horizontalExtras
        if (measuring > 0) {
            return width * factor
        }

        shift(start, border.left + padding.left, 0.0)
        style.backgroundColor?.let { background ->
            prefix.add(Item(Kind.RECT, 0.0, top, width = width, height = bottom - top, color = background))
        }
        borderRects(span, border, width, top, bottom, color)
        if (prefix.isNotEmpty()) {
            out.addAll(start, prefix)
            prefix.clear()
        }
        val svgWidth = (explicitWidth ?: fill?.minus(horizontalExtras) ?: contentWidth) + horizontalExtras
        absoluteChildren(span, context, width, svgWidth, top, bottom)

        if (style.textShadow != null) {
            val end = out.size
            (start until end).forEach { i ->
                val item = out[i]
                if (item.kind == Kind.GLYPH) {
                    out.add(Item(Kind.GLYPH, item.x + 0.02, item.y + 0.01, size = item.size, text = item.text, fontName = item.fontName, color = item.color, mirrored = item.mirrored))
                }
            }
        }
        if (span.has(REFLECTBOX)) {
            mirror(start, width)
        }

        var dx = em(style.left) ?: 0.0
        var dy = (em(style.top) ?: 0.0) - (em(style.bottom) ?: 0.0) - (em(style.verticalAlign) ?: 0.0)
        if (factor != 1.0) {
            scale(start, factor)
            dx *= factor
            dy *= factor
        }
        shift(start, dx, dy)
        return width * factor
    }

    /** KaTeX numbers equations with a CSS counter, `.eqn-num::before { content: "(" counter ")" }`. */
    private fun equationNumber(span: Span, context: Context): Double {
        val number = equationNumbers.getOrPut(span) { equationNumbers.size + 1 }
        var x = 0.0
        "($number)".forEach { char ->
            val text = char.toString()
            if (measuring == 0 && context.color != "transparent") {
                out.add(Item(Kind.GLYPH, x, 0.0, text = text, fontName = "Main-Regular", color = context.color))
            }
            x += getCharacterMetrics(text, "Main-Regular", Mode.TEXT)?.width ?: measurer.measure(text, "Main-Regular")
        }
        return x
    }

    /**
     * An SVG with no width of its own fills its nearest positioned ancestor, which for KaTeX is
     * usually the vlist row, hence [svgWidth] rather than the span's own [width].
     */
    private fun absoluteChildren(span: Span, context: Context, width: Double, svgWidth: Double, top: Double, bottom: Double) {
        span.children.forEach { child ->
            when {
                child is SvgNode -> layoutSvg(child, span, context, svgWidth, top)
                child is Span && child.has(KATEX_INNER) -> {
                    val start = out.size
                    val laidWidth = layoutSpan(child, context, null)
                    val x =
                        when {
                            span.has(LLAP) -> -laidWidth
                            span.has(CLAP) -> -laidWidth / 2
                            else -> 0.0
                        }
                    shift(start, x, 0.0)
                }
                child is Span && child.has(PERCENT_PIECE) -> {
                    val (left, pieceWidth) = percentPiece(child, width)
                    val pieceContext = context.copy(clip = TexItem.Clip(left, top, pieceWidth, bottom - top))
                    val start = out.size
                    layoutSpan(child, pieceContext, pieceWidth)
                    shift(start, left, 0.0)
                }
            }
        }
    }

    private fun layoutSvg(svg: SvgNode, parent: Span, context: Context, parentWidth: Double, parentTop: Double) {
        val styleWidth = svg.attributes["style"]?.substringAfter("width:", "")?.let { em(it) }
        val width = styleWidth ?: parentWidth
        val height = em(parent.style.height) ?: em(svg.attributes["height"]) ?: 0.0
        val top = if (parent.style.height != null) parentTop else -height
        val color = context.color
        if (color == "transparent") {
            return
        }
        val paths = svg.children.filterIsInstance<PathNode>().mapNotNull { it.alternate ?: SvgGeometry.path[it.pathName] }
        if (paths.isNotEmpty()) {
            val viewBox = svg.attributes["viewBox"]?.trim()?.split(viewBoxSeparator)?.map { it.toDouble() }
            val clip = context.clip?.let { doubleArrayOf(it.x, it.y, it.width, it.height) }
            out.add(
                Item(
                    Kind.SVG,
                    0.0,
                    top,
                    width = width,
                    height = height,
                    viewBox = viewBox,
                    preserveAspectRatio = svg.attributes["preserveAspectRatio"],
                    paths = paths,
                    clip = clip,
                    color = color,
                ),
            )
        }
        svg.children.forEach { child ->
            val line = child as? LineNode ?: return@forEach
            fun coordinate(value: String?, extent: Double): Double =
                when {
                    value == null -> 0.0
                    value.endsWith("%") -> value.dropLast(1).toDouble() / 100 * extent
                    else -> em(value) ?: value.toDoubleOrNull()?.div(1000) ?: 0.0
                }
            out.add(
                Item(
                    Kind.LINE,
                    coordinate(line.attributes["x1"], width),
                    top + coordinate(line.attributes["y1"], height),
                    x2 = coordinate(line.attributes["x2"], width),
                    y2 = top + coordinate(line.attributes["y2"], height),
                    strokeWidth = em(line.attributes["stroke-width"]) ?: 0.04,
                    color = color,
                ),
            )
        }
    }

    private fun percentPiece(piece: Span, parentWidth: Double): Pair<Double, Double> =
        when {
            piece.has(HALFARROW_LEFT) -> 0.0 to parentWidth * 0.502
            piece.has(HALFARROW_RIGHT) -> parentWidth * (1 - 0.502) to parentWidth * 0.502
            piece.has(BRACE_LEFT) -> 0.0 to parentWidth * 0.251
            piece.has(BRACE_CENTER) -> parentWidth * 0.25 to parentWidth * 0.5
            else -> parentWidth * (1 - 0.251) to parentWidth * 0.251
        }

    private fun isAbsolute(node: VirtualNode): Boolean =
        node is Span && (node.has(KATEX_INNER) || node.has(PERCENT_PIECE))

    private fun isBlock(span: Span): Boolean = span.has(BLOCK)

    private fun explicitWidth(span: Span, fill: Double?, horizontalExtras: Double): Double? {
        val width = span.style.width
        if (width == null) {
            if (span.has(FULL_WIDTH)) {
                return fill?.minus(horizontalExtras) ?: 0.0
            }
            return null
        }
        if (width.endsWith("%")) {
            return (fill ?: 0.0) * width.dropLast(1).toDouble() / 100 - horizontalExtras
        }
        val value = em(width) ?: return null
        return if (span.has(BORDER_BOX)) value - horizontalExtras else value
    }

    private fun boxTop(span: Span): Double = em(span.style.height)?.let { -it } ?: -span.height

    private fun boxBottom(span: Span): Double = if (span.style.height != null) 0.0 else span.depth

    private fun sizingFactor(span: Span): Double {
        if (!span.has(KATEX_SIZING) && !span.has(FONTSIZE_ENSURER)) {
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
            node.has(MSUPSUB) || node.has(SVG_ALIGN) || node.has(COL_ALIGN_L) -> Align.LEFT
            node.has(COL_ALIGN_R) -> Align.RIGHT
            node.has(CENTERING) -> Align.CENTER
            else -> inherited
        }

    /** Sets [margin]; callers copy it out before laying out anything else. */
    private fun margins(node: VirtualNode) {
        if (node is SymbolNode) {
            margin.left = em(node.style.marginLeft) ?: 0.0
            margin.right = em(node.style.marginRight) ?: 0.0
            return
        }
        if (node !is Span) {
            margin.left = 0.0
            margin.right = 0.0
            return
        }
        val hasMarginClass = node.has(MARGIN_CLASS)
        var left = if (hasMarginClass) node.classes.sumOf { classMarginLeft[it] ?: 0.0 } else 0.0
        var right = if (hasMarginClass) node.classes.sumOf { classMarginRight[it] ?: 0.0 } else 0.0
        node.style.margin?.let { cssMargin ->
            val parts = cssMargin.trim().split(whitespace).map { em(it) ?: 0.0 }
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
        margin.left = left * factor
        margin.right = right * factor
    }

    private fun paddings(span: Span): Edges {
        val hasPaddingClass = span.has(PADDING_CLASS)
        var left = if (hasPaddingClass) span.classes.sumOf { classPaddingLeft[it] ?: 0.0 } else 0.0
        val right = if (hasPaddingClass) span.classes.sumOf { classPaddingRight[it] ?: 0.0 } else 0.0
        em(span.style.paddingLeft)?.let { left = it }
        return Edges(left = left, right = right)
    }

    private fun borders(span: Span): Edges {
        val style = span.style
        val all = em(style.borderWidth)
        return when {
            span.has(FBOX) || span.has(FCOLORBOX) -> {
                val width = all ?: 0.04
                Edges(width, width, width, width)
            }
            span.has(ANGL) -> Edges(right = all ?: 0.049, top = all ?: 0.049)
            span.has(KATEX_SOUT) -> Edges(bottom = em(style.borderBottomWidth) ?: 0.08)
            span.has(BOTTOM_RULE) -> Edges(bottom = em(style.borderBottomWidth) ?: 0.0)
            else ->
                Edges(
                    left = all ?: 0.0,
                    right = em(style.borderRightWidth) ?: all ?: 0.0,
                    top = em(style.borderTopWidth) ?: all ?: 0.0,
                    bottom = em(style.borderBottomWidth) ?: all ?: 0.0,
                )
        }
    }

    /** Appends the border rules to [prefix], which [layoutSpan] places before the content. */
    private fun borderRects(span: Span, border: Edges, width: Double, top: Double, bottom: Double, color: String?) {
        val borderColor = span.style.borderColor ?: color
        if (borderColor == "transparent") {
            return
        }
        val dashed = span.has(KATEX_HDASHLINE) || span.style.borderRightStyle == "dashed" || span.style.borderStyle == "dashed"
        val hasContent = span.children.isNotEmpty()
        val boxTop = if (!hasContent && span.style.height == null) -(border.top + border.bottom) else top
        val boxBottom = if (!hasContent && span.style.height == null) 0.0 else bottom
        if (border.top > 0) {
            prefix.add(Item(Kind.RECT, 0.0, boxTop, width = width, height = border.top, color = borderColor, dashed = dashed))
        }
        if (border.bottom > 0) {
            prefix.add(Item(Kind.RECT, 0.0, boxBottom - border.bottom, width = width, height = border.bottom, color = borderColor, dashed = dashed))
        }
        if (border.left > 0) {
            prefix.add(Item(Kind.RECT, 0.0, boxTop, width = border.left, height = boxBottom - boxTop, color = borderColor, dashed = dashed))
        }
        if (border.right > 0) {
            prefix.add(Item(Kind.RECT, width - border.right, boxTop, width = border.right, height = boxBottom - boxTop, color = borderColor, dashed = dashed))
        }
    }

    private fun shift(start: Int, dx: Double, dy: Double) {
        if (dx == 0.0 && dy == 0.0) {
            return
        }
        (start until out.size).forEach { i ->
            val item = out[i]
            item.x += dx
            item.y += dy
            if (item.kind == Kind.LINE) {
                item.x2 += dx
                item.y2 += dy
            }
            item.clip?.let { clip ->
                clip[0] += dx
                clip[1] += dy
            }
        }
    }

    private fun scale(start: Int, factor: Double) {
        (start until out.size).forEach { i ->
            val item = out[i]
            item.x *= factor
            item.y *= factor
            when (item.kind) {
                Kind.GLYPH -> item.size *= factor
                Kind.RECT, Kind.SVG -> {
                    item.width *= factor
                    item.height *= factor
                }
                Kind.LINE -> {
                    item.x2 *= factor
                    item.y2 *= factor
                    item.strokeWidth *= factor
                }
            }
            item.clip?.let { clip ->
                clip[0] *= factor
                clip[1] *= factor
                clip[2] *= factor
                clip[3] *= factor
            }
        }
    }

    private fun mirror(start: Int, width: Double) {
        (start until out.size).forEach { i ->
            val item = out[i]
            when (item.kind) {
                Kind.GLYPH -> {
                    item.x = width - item.x
                    item.mirrored = !item.mirrored
                }
                Kind.RECT -> item.x = width - item.x - item.width
                Kind.SVG -> Unit
                Kind.LINE -> {
                    item.x = width - item.x
                    item.x2 = width - item.x2
                }
            }
        }
    }

    /**
     * A browser sizes a glyph's span by the font's advance, not KaTeX's metrics. For the
     * multiple integrals KaTeX reuses \int's metric width, but the glyphs are much wider.
     */
    private fun advanceOverride(fontName: String, text: String): Double? =
        when (text) {
            "∬" -> if (fontName == "Size2-Regular") 1.084 else if (fontName == "Size1-Regular") 0.819 else null
            "∭" -> if (fontName == "Size2-Regular") 1.592 else if (fontName == "Size1-Regular") 1.166 else null
            else -> null
        }

    /** Whether this span has any class in [bits], from a mask built once per span. */
    private fun Span.has(bits: Long): Boolean {
        if (layoutFlags == UNSET_LAYOUT_FLAGS) {
            var flags = 0L
            classes.forEach { flags = flags or (classFlags[it] ?: 0L) }
            layoutFlags = flags
        }
        return layoutFlags and bits != 0L
    }

    private companion object {
        val whitespace = Regex("\\s+")
        val viewBoxSeparator = Regex("[\\s,]+")

        val emRegex = Regex("^\\s*(-?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?)em\\s*$")

        fun em(value: String?): Double? {
            if (value == null) {
                return null
            }
            if (value.endsWith("em")) {
                try {
                    return java.lang.Double.parseDouble(value.substring(0, value.length - 2))
                } catch (_: NumberFormatException) {
                    // Fall through to the regex for padded or unusual values.
                }
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

        const val KATEX_INNER = 1L shl 0
        const val SVG_ALIGN = 1L shl 1
        const val REFLECTBOX = 1L shl 2
        const val NULLDELIMITER = 1L shl 3
        const val MSUPSUB = 1L shl 4
        const val LLAP = 1L shl 5
        const val KATEX_TAG = 1L shl 6
        const val KATEX_SOUT = 1L shl 7
        const val KATEX_SIZING = 1L shl 8
        const val KATEX_HDASHLINE = 1L shl 9
        const val HALFARROW_RIGHT = 1L shl 10
        const val HALFARROW_LEFT = 1L shl 11
        const val FONTSIZE_ENSURER = 1L shl 12
        const val FCOLORBOX = 1L shl 13
        const val FBOX = 1L shl 14
        const val EQN_NUM = 1L shl 15
        const val COL_ALIGN_R = 1L shl 16
        const val COL_ALIGN_L = 1L shl 17
        const val CLAP = 1L shl 18
        const val BRACE_LEFT = 1L shl 19
        const val BRACE_CENTER = 1L shl 20
        const val ANGL = 1L shl 21
        const val ACCENT_FULL = 1L shl 22
        const val OVERFLOW_HIDDEN = 1L shl 23
        const val ZERO_WIDTH = 1L shl 24
        const val PERCENT_PIECE = 1L shl 25
        const val BLOCK = 1L shl 26
        const val FULL_WIDTH = 1L shl 27
        const val BORDER_BOX = 1L shl 28
        const val CENTERING = 1L shl 29
        const val BOTTOM_RULE = 1L shl 30
        const val MARGIN_CLASS = 1L shl 31
        const val PADDING_CLASS = 1L shl 32

        /** Every class [TexLayout] checks, mapped to its bits; set-membership bits are or-ed in. */
        val classFlags: Map<String, Long> =
            HashMap<String, Long>().apply {
                fun add(names: Iterable<String>, bit: Long) = names.forEach { put(it, (get(it) ?: 0L) or bit) }
                add(listOf("katex-inner"), KATEX_INNER)
                add(listOf("svg-align"), SVG_ALIGN)
                add(listOf("reflectbox"), REFLECTBOX)
                add(listOf("nulldelimiter"), NULLDELIMITER)
                add(listOf("msupsub"), MSUPSUB)
                add(listOf("llap"), LLAP)
                add(listOf("katex-tag"), KATEX_TAG)
                add(listOf("katex-sout"), KATEX_SOUT)
                add(listOf("katex-sizing"), KATEX_SIZING)
                add(listOf("katex-hdashline"), KATEX_HDASHLINE)
                add(listOf("halfarrow-right"), HALFARROW_RIGHT)
                add(listOf("halfarrow-left"), HALFARROW_LEFT)
                add(listOf("fontsize-ensurer"), FONTSIZE_ENSURER)
                add(listOf("fcolorbox"), FCOLORBOX)
                add(listOf("fbox"), FBOX)
                add(listOf("eqn-num"), EQN_NUM)
                add(listOf("col-align-r"), COL_ALIGN_R)
                add(listOf("col-align-l"), COL_ALIGN_L)
                add(listOf("clap"), CLAP)
                add(listOf("brace-left"), BRACE_LEFT)
                add(listOf("brace-center"), BRACE_CENTER)
                add(listOf("angl"), ANGL)
                add(listOf("accent-full"), ACCENT_FULL)
                add(overflowHidden, OVERFLOW_HIDDEN)
                add(zeroWidthClasses, ZERO_WIDTH)
                add(percentPieces, PERCENT_PIECE)
                add(blockClasses, BLOCK)
                add(fullWidthClasses, FULL_WIDTH)
                add(borderBoxClasses, BORDER_BOX)
                add(centeringClasses, CENTERING)
                add(bottomRuleClasses, BOTTOM_RULE)
                add(classMarginLeft.keys + classMarginRight.keys, MARGIN_CLASS)
                add(classPaddingLeft.keys + classPaddingRight.keys, PADDING_CLASS)
            }
    }
}
