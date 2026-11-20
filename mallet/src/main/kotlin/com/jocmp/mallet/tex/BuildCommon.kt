package com.jocmp.mallet.tex

private val ligatures = setOf("--", "---", "``", "''")

class FontInfo(
    val variant: String,
    val fontName: String,
)

val fontMap: Map<String, FontInfo> =
    mapOf(
        "mathbf" to FontInfo("bold", "Main-Bold"),
        "mathrm" to FontInfo("normal", "Main-Regular"),
        "textit" to FontInfo("italic", "Main-Italic"),
        "mathit" to FontInfo("italic", "Main-Italic"),
        "mathnormal" to FontInfo("italic", "Math-Italic"),
        "mathsfit" to FontInfo("sans-serif-italic", "SansSerif-Italic"),
        "mathbb" to FontInfo("double-struck", "AMS-Regular"),
        "mathcal" to FontInfo("script", "Caligraphic-Regular"),
        "mathfrak" to FontInfo("fraktur", "Fraktur-Regular"),
        "mathscr" to FontInfo("script", "Script-Regular"),
        "mathsf" to FontInfo("sans-serif", "SansSerif-Regular"),
        "mathtt" to FontInfo("monospace", "Typewriter-Regular"),
    )

class SvgData(
    val pathName: String,
    val width: Double,
    val height: Double,
)

val svgData: Map<String, SvgData> =
    mapOf(
        "vec" to SvgData("vec", 0.471, 0.714),
        "oiintSize1" to SvgData("oiintSize1", 0.957, 0.499),
        "oiintSize2" to SvgData("oiintSize2", 1.472, 0.659),
        "oiiintSize1" to SvgData("oiiintSize1", 1.304, 0.499),
        "oiiintSize2" to SvgData("oiiintSize2", 1.98, 0.659),
    )

private class SymbolLookup(
    val value: String,
    val metrics: CharacterMetrics?,
)

private fun lookupSymbol(value: String, fontName: String, mode: Mode): SymbolLookup {
    val replacement = Symbols[mode][value]?.replace
    val resolved = if (!replacement.isNullOrEmpty()) replacement else value
    return SymbolLookup(resolved, getCharacterMetrics(resolved, fontName, mode))
}

/** A symbol in the given font, sized by KaTeX's metrics for that font. */
fun makeSymbol(
    value: String,
    fontName: String,
    mode: Mode,
    options: Options?,
    classes: List<String> = emptyList(),
): SymbolNode {
    val lookup = lookupSymbol(value, fontName, mode)
    val metrics = lookup.metrics
    val symbolNode =
        if (metrics != null) {
            val italic = if (mode == Mode.TEXT || options?.font == "mathit") 0.0 else metrics.italic
            SymbolNode(lookup.value, metrics.height, metrics.depth, italic, metrics.skew, metrics.width, classes)
                .also { it.hasMetrics = true }
        } else {
            SymbolNode(lookup.value, 0.0, 0.0, 0.0, 0.0, 0.0, classes)
        }
    symbolNode.fontName = fontName
    if (options != null) {
        symbolNode.maxFontSize = options.sizeMultiplier
        if (options.style.isTight()) {
            symbolNode.classes.add("mtight")
        }
        options.getColor()?.let { symbolNode.style.color = it }
    }
    return symbolNode
}

/** A symbol in the main or AMS math fonts. */
fun mathsym(value: String, mode: Mode, options: Options, classes: List<String> = emptyList()): SymbolNode =
    if (options.font == "boldsymbol" && lookupSymbol(value, "Main-Bold", mode).metrics != null) {
        makeSymbol(value, "Main-Bold", mode, options, classes + "mathbf")
    } else if (value == "\\" || Symbols[mode][value]?.font == "main") {
        makeSymbol(value, "Main-Regular", mode, options, classes)
    } else {
        makeSymbol(value, "AMS-Regular", mode, options, classes + "amsrm")
    }

private fun boldSymbol(value: String, mode: Mode, type: String): Pair<String, String> =
    if (type != "textord" && lookupSymbol(value, "Math-BoldItalic", mode).metrics != null) {
        "Math-BoldItalic" to "boldsymbol"
    } else {
        "Main-Bold" to "mathbf"
    }

/** Builds a mathord, textord, or spacing symbol in the current font. */
fun makeOrd(group: ParseNode.Symbol, options: Options): HtmlDomNode {
    val type = if (group is ParseNode.MathOrd) "mathord" else "textord"
    val mode = group.mode
    val text = group.text
    val classes = listOf("mord")
    val font = options.font
    val fontFamily = options.fontFamily
    val fontWeight = options.fontWeight
    val fontShape = options.fontShape

    val useFont = mode == Mode.MATH || (mode == Mode.TEXT && font.isNotEmpty())
    val fontOrFamily = if (useFont) font else fontFamily
    var wideFontName = ""
    var wideFontClass = ""
    if (text.firstOrNull()?.code == 0xD835) {
        val wideCharData = wideCharacterFont(text)
        wideFontName = wideCharData.font
        wideFontClass = if (mode == Mode.MATH) wideCharData.mathClass else wideCharData.textClass
    }

    if (wideFontName.isNotEmpty()) {
        return makeSymbol(text, wideFontName, mode, options, classes + wideFontClass)
    } else if (fontOrFamily.isNotEmpty()) {
        val fontName: String
        val fontClasses: List<String>
        if (fontOrFamily == "boldsymbol") {
            val (name, fontClass) = boldSymbol(text, mode, type)
            fontName = name
            fontClasses = listOf(fontClass)
        } else if (useFont) {
            fontName = fontMap.getValue(font).fontName
            fontClasses = listOf(font)
        } else {
            fontName = retrieveTextFontName(fontFamily, fontWeight, fontShape)
            fontClasses = listOf(fontFamily, fontWeight, fontShape)
        }

        if (lookupSymbol(text, fontName, mode).metrics != null) {
            return makeSymbol(text, fontName, mode, options, classes + fontClasses)
        } else if (text in ligatures && fontName.startsWith("Typewriter")) {
            val parts = text.map { makeSymbol(it.toString(), fontName, mode, options, classes + fontClasses) }
            return makeFragment(parts)
        }
    }

    if (type == "mathord") {
        return makeSymbol(text, "Math-Italic", mode, options, classes + "mathnormal")
    }
    val symbolFont = Symbols[mode][text]?.font
    return if (symbolFont == "ams") {
        val fontName = retrieveTextFontName("amsrm", fontWeight, fontShape)
        makeSymbol(text, fontName, mode, options, classes + listOf("amsrm", fontWeight, fontShape))
    } else {
        val fontName = retrieveTextFontName("textrm", fontWeight, fontShape)
        makeSymbol(text, fontName, mode, options, classes + listOf(fontWeight, fontShape))
    }
}

/**
 * KaTeX merges adjacent symbols to shrink the DOM and lets the browser measure the result.
 * Merging would lose per-glyph metrics here, so this keeps the symbols separate.
 */
fun tryCombineChars(chars: MutableList<HtmlDomNode>): MutableList<HtmlDomNode> = chars

private fun sizeElementFromChildren(elem: HtmlDomNode, children: List<VirtualNode>) {
    var height = 0.0
    var depth = 0.0
    var maxFontSize = 0.0
    children.filterIsInstance<HtmlDomNode>().forEach { child ->
        height = maxOf(height, child.height)
        depth = maxOf(depth, child.depth)
        maxFontSize = maxOf(maxFontSize, child.maxFontSize)
    }
    elem.height = height
    elem.depth = depth
    elem.maxFontSize = maxFontSize
}

fun makeSpan(
    classes: List<String> = emptyList(),
    children: List<HtmlDomNode> = emptyList(),
    options: Options? = null,
    style: CssStyle? = null,
): Span {
    val span = Span(classes, children, options, style)
    sizeElementFromChildren(span, span.children)
    return span
}

fun makeSvgSpan(
    classes: List<String> = emptyList(),
    children: List<SvgNode> = emptyList(),
    options: Options? = null,
    style: CssStyle? = null,
): Span = Span(classes, children, options, style)

fun makeLineSpan(className: String, options: Options, thickness: Double? = null): Span {
    val line = makeSpan(listOf(className), emptyList(), options)
    val ruleThickness = if (thickness != null && thickness != 0.0) thickness else options.fontMetrics().defaultRuleThickness
    line.height = maxOf(ruleThickness, options.minRuleThickness)
    line.style.borderBottomWidth = makeEm(line.height)
    line.maxFontSize = 1.0
    return line
}

fun makeAnchor(href: String, classes: List<String>, children: List<HtmlDomNode>, options: Options): Anchor {
    val anchor = Anchor(href, classes, children, options)
    sizeElementFromChildren(anchor, anchor.children)
    return anchor
}

fun makeFragment(children: List<HtmlDomNode>): DocumentFragment {
    val fragment = DocumentFragment(children)
    sizeElementFromChildren(fragment, fragment.children)
    return fragment
}

fun wrapFragment(group: HtmlDomNode, options: Options): HtmlDomNode =
    if (group is DocumentFragment) makeSpan(emptyList(), listOf(group), options) else group

sealed interface VListChild {
    data class Kern(val size: Double) : VListChild

    data class Elem(
        val elem: HtmlDomNode,
        val shift: Double = 0.0,
        val marginLeft: String? = null,
        val marginRight: String? = null,
        val wrapperClasses: List<String> = emptyList(),
        val wrapperStyle: CssStyle? = null,
    ) : VListChild
}

sealed interface VListParam {
    /** Each child's [VListChild.Elem.shift] is its baseline offset, positive downward. */
    data class IndividualShift(val children: List<VListChild.Elem>) : VListParam

    data class Top(val positionData: Double, val children: List<VListChild>) : VListParam

    data class Bottom(val positionData: Double, val children: List<VListChild>) : VListParam

    data class Shift(val positionData: Double, val children: List<VListChild>) : VListParam

    data class FirstBaseline(val children: List<VListChild>) : VListParam
}

private fun getVListChildrenAndDepth(params: VListParam): Pair<List<VListChild>, Double> {
    when (params) {
        is VListParam.IndividualShift -> {
            val oldChildren = params.children
            val children = mutableListOf<VListChild>(oldChildren[0])
            val depth = -oldChildren[0].shift - oldChildren[0].elem.depth
            var currPos = depth
            (1 until oldChildren.size).forEach { i ->
                val diff = -oldChildren[i].shift - currPos - oldChildren[i].elem.depth
                val size = diff - (oldChildren[i - 1].elem.height + oldChildren[i - 1].elem.depth)
                currPos += diff
                children.add(VListChild.Kern(size))
                children.add(oldChildren[i])
            }
            return children to depth
        }
        is VListParam.Top -> {
            var bottom = params.positionData
            params.children.forEach { child ->
                bottom -=
                    when (child) {
                        is VListChild.Kern -> child.size
                        is VListChild.Elem -> child.elem.height + child.elem.depth
                    }
            }
            return params.children to bottom
        }
        is VListParam.Bottom -> return params.children to -params.positionData
        is VListParam.Shift -> {
            val first = params.children.first() as? VListChild.Elem ?: throw IllegalStateException("First child must have type \"elem\".")
            return params.children to -first.elem.depth - params.positionData
        }
        is VListParam.FirstBaseline -> {
            val first = params.children.first() as? VListChild.Elem ?: throw IllegalStateException("First child must have type \"elem\".")
            return params.children to -first.elem.depth
        }
    }
}

/**
 * Stacks children vertically, as KaTeX's makeVList. Positions are resolved into [VListEntry]
 * baselines rather than pstrut offsets.
 */
fun makeVList(params: VListParam, @Suppress("UNUSED_PARAMETER") options: Options? = null): VListSpan {
    val (children, depth) = getVListChildrenAndDepth(params)

    val entries = mutableListOf<VListEntry>()
    var minPos = depth
    var maxPos = depth
    var currPos = depth
    children.forEach { child ->
        when (child) {
            is VListChild.Kern -> currPos += child.size
            is VListChild.Elem -> {
                val elem = child.elem
                val wrapper = makeSpan(child.wrapperClasses, listOf(elem), null, child.wrapperStyle?.copy())
                child.marginLeft?.takeIf { it.isNotEmpty() }?.let { wrapper.style.marginLeft = it }
                child.marginRight?.takeIf { it.isNotEmpty() }?.let { wrapper.style.marginRight = it }
                entries.add(VListEntry(elem, wrapper, baseline = -(currPos + elem.depth)))
                currPos += elem.height + elem.depth
            }
        }
        minPos = minOf(minPos, currPos)
        maxPos = maxOf(maxPos, currPos)
    }

    val vtable = VListSpan(entries)
    if (minPos < 0) {
        vtable.classes.add("vlist-t2")
    }
    vtable.height = maxPos
    vtable.depth = -minPos
    return vtable
}

/** Horizontal space, as KaTeX's mspace. */
fun makeGlue(measurement: Measurement, options: Options): Span {
    val rule = makeSpan(listOf("mspace"), emptyList(), options)
    val size = calculateSize(measurement, options)
    rule.style.marginRight = makeEm(size)
    return rule
}

private fun retrieveTextFontName(fontFamily: String, fontWeight: String, fontShape: String): String {
    val baseFontName =
        when (fontFamily) {
            "amsrm" -> "AMS"
            "textrm" -> "Main"
            "textsf" -> "SansSerif"
            "texttt" -> "Typewriter"
            else -> fontFamily
        }
    val fontStylesName =
        when {
            fontWeight == "textbf" && fontShape == "textit" -> "BoldItalic"
            fontWeight == "textbf" -> "Bold"
            fontShape == "textit" -> "Italic"
            else -> "Regular"
        }
    return "$baseFontName-$fontStylesName"
}

fun staticSvg(value: String, options: Options): Span {
    val data = svgData.getValue(value)
    val path = PathNode(data.pathName)
    val svgNode =
        SvgNode(
            listOf(path),
            mutableMapOf(
                "width" to makeEm(data.width),
                "height" to makeEm(data.height),
                "style" to "width:" + makeEm(data.width),
                "viewBox" to "0 0 ${svgNumber(1000 * data.width)} ${svgNumber(1000 * data.height)}",
                "preserveAspectRatio" to "xMinYMin",
            ),
        )
    val span = makeSvgSpan(listOf("katex-overlay"), listOf(svgNode), options)
    span.height = data.height
    span.style.height = makeEm(data.height)
    span.style.width = makeEm(data.width)
    return span
}

/** JS number-to-string for SVG attributes: integers print without a decimal point. */
fun svgNumber(n: Double): String = if (n == Math.floor(n) && !n.isInfinite()) n.toLong().toString() else n.toString()

class WideChar(
    val mathClass: String,
    val textClass: String,
    val font: String,
)

private val boldUpright = WideChar("mathbf", "textbf", "Main-Bold")
private val italicWide = WideChar("mathnormal", "textit", "Math-Italic")
private val boldItalic = WideChar("boldsymbol", "boldsymbol", "Main-BoldItalic")
private val scriptWide = WideChar("mathscr", "textscr", "Script-Regular")
private val noFont = WideChar("", "", "")
private val frakturWide = WideChar("mathfrak", "textfrak", "Fraktur-Regular")
private val doubleStruck = WideChar("mathbb", "textbb", "AMS-Regular")
private val boldFraktur = WideChar("mathboldfrak", "textboldfrak", "Fraktur-Regular")
private val sansSerif = WideChar("mathsf", "textsf", "SansSerif-Regular")
private val boldSansSerif = WideChar("mathboldsf", "textboldsf", "SansSerif-Bold")
private val italicSansSerif = WideChar("mathitsf", "textitsf", "SansSerif-Italic")
private val monospace = WideChar("mathtt", "texttt", "Typewriter-Regular")

private val wideLatinLetterData =
    listOf(
        boldUpright, boldUpright,
        italicWide, italicWide,
        boldItalic, boldItalic,
        scriptWide, noFont,
        noFont, noFont,
        frakturWide, frakturWide,
        doubleStruck, doubleStruck,
        boldFraktur, boldFraktur,
        sansSerif, sansSerif,
        boldSansSerif, boldSansSerif,
        italicSansSerif, italicSansSerif,
        noFont, noFont,
        monospace, monospace,
    )

private val wideNumeralData = listOf(boldUpright, noFont, sansSerif, boldSansSerif, monospace)

fun wideCharacterFont(wideChar: String): WideChar {
    val codePoint = wideChar.codePointAt(0)
    return when {
        codePoint in 0x1D400 until 0x1D6A4 -> wideLatinLetterData[(codePoint - 0x1D400) / 26]
        codePoint in 0x1D7CE..0x1D7FF -> wideNumeralData[(codePoint - 0x1D7CE) / 10]
        codePoint == 0x1D6A5 || codePoint == 0x1D6A6 -> wideLatinLetterData[0]
        codePoint in 0x1D6A7 until 0x1D7CE -> noFont
        else -> throw ParseError("Unsupported character: $wideChar")
    }
}
