package com.jocmp.mallet.tex

class SqrtImage(
    val span: Span,
    val ruleWidth: Double,
    val advanceWidth: Double,
)

/**
 * KaTeX's delimiter.ts: delimiters of various sizes, per the TeXbook's "Another subroutine sets
 * box x to a specified variable delimiter" (pp. 441-442).
 */
object Delimiter {
    /** Metrics of the different sizes, from TeX's output of `\bigl| \Bigl| \biggl| \Biggl|`. */
    val sizeToMaxHeight: List<Double> = listOf(0.0, 1.2, 1.8, 2.4, 3.0)

    fun makeSqrtImage(height: Double, options: Options): SqrtImage {
        val newOptions = options.havingBaseSizing()

        val delim = traverseSequence("\\surd", height * newOptions.sizeMultiplier, stackLargeDelimiterSequence, newOptions)

        var sizeMultiplier = newOptions.sizeMultiplier

        val extraVinculum = maxOf(0.0, options.minRuleThickness - options.fontMetrics().sqrtRuleThickness)

        val span: Span
        val spanHeight: Double
        val texHeight: Double
        val viewBoxHeight: Double
        val advanceWidth: Double

        if (delim is DelimType.Small) {
            viewBoxHeight = 1000 + 1000 * extraVinculum + VB_PAD
            if (height < 1.0) {
                sizeMultiplier = 1.0
            } else if (height < 1.4) {
                sizeMultiplier = 0.7
            }
            spanHeight = (1.0 + extraVinculum + EM_PAD) / sizeMultiplier
            texHeight = (1.00 + extraVinculum) / sizeMultiplier
            span = sqrtSvg("sqrtMain", spanHeight, viewBoxHeight, extraVinculum, options)
            span.style.minWidth = "0.853em"
            advanceWidth = 0.833 / sizeMultiplier
        } else if (delim is DelimType.Large) {
            viewBoxHeight = (1000 + VB_PAD) * sizeToMaxHeight[delim.size]
            texHeight = (sizeToMaxHeight[delim.size] + extraVinculum) / sizeMultiplier
            spanHeight = (sizeToMaxHeight[delim.size] + extraVinculum + EM_PAD) / sizeMultiplier
            span = sqrtSvg("sqrtSize" + delim.size, spanHeight, viewBoxHeight, extraVinculum, options)
            span.style.minWidth = "1.02em"
            advanceWidth = 1.0 / sizeMultiplier
        } else {
            spanHeight = height + extraVinculum + EM_PAD
            texHeight = height + extraVinculum
            viewBoxHeight = Math.floor(1000 * height + extraVinculum) + VB_PAD
            span = sqrtSvg("sqrtTall", spanHeight, viewBoxHeight, extraVinculum, options)
            span.style.minWidth = "0.742em"
            advanceWidth = 1.056
        }

        span.height = texHeight
        span.style.height = makeEm(spanHeight)

        return SqrtImage(
            span = span,
            advanceWidth = advanceWidth,
            ruleWidth = (options.fontMetrics().sqrtRuleThickness + extraVinculum) * sizeMultiplier,
        )
    }

    /** A delimiter of a specific size, where [size] is 1, 2, 3, or 4. */
    fun makeSizedDelim(delim: String, size: Int, options: Options, mode: Mode, classes: List<String>): Span {
        var delimiter = delim
        if (delimiter == "<" || delimiter == "\\lt" || delimiter == "⟨") {
            delimiter = "\\langle"
        } else if (delimiter == ">" || delimiter == "\\gt" || delimiter == "⟩") {
            delimiter = "\\rangle"
        }

        if (delimiter in stackLargeDelimiters || delimiter in stackNeverDelimiters) {
            return makeLargeDelim(delimiter, size, false, options, mode, classes)
        } else if (delimiter in stackAlwaysDelimiters) {
            return makeStackedDelim(delimiter, sizeToMaxHeight[size], false, options, mode, classes)
        } else {
            throw ParseError("Illegal delimiter: '$delimiter'")
        }
    }

    /** A delimiter of a given height+depth, with optional centering around the axis. */
    fun makeCustomSizedDelim(
        delim: String,
        height: Double,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: List<String>,
    ): Span {
        var delimiter = delim
        if (delimiter == "<" || delimiter == "\\lt" || delimiter == "⟨") {
            delimiter = "\\langle"
        } else if (delimiter == ">" || delimiter == "\\gt" || delimiter == "⟩") {
            delimiter = "\\rangle"
        }

        val sequence =
            if (delimiter in stackNeverDelimiters) {
                stackNeverDelimiterSequence
            } else if (delimiter in stackLargeDelimiters) {
                stackLargeDelimiterSequence
            } else {
                stackAlwaysDelimiterSequence
            }

        val delimType = traverseSequence(delimiter, height, sequence, options)

        return when (delimType) {
            is DelimType.Small -> makeSmallDelim(delimiter, delimType.style, center, options, mode, classes)
            is DelimType.Large -> makeLargeDelim(delimiter, delimType.size, center, options, mode, classes)
            DelimType.Stack -> makeStackedDelim(delimiter, height, center, options, mode, classes)
        }
    }

    /** A `\left`/`\right` delimiter surrounding an expression of the given height and depth. */
    fun makeLeftRightDelim(
        delim: String,
        height: Double,
        depth: Double,
        options: Options,
        mode: Mode,
        classes: List<String>,
    ): Span {
        val axisHeight = options.fontMetrics().axisHeight * options.sizeMultiplier

        val delimiterFactor = 901
        val delimiterExtend = 5.0 / options.fontMetrics().ptPerEm

        val maxDistFromAxis = maxOf(height - axisHeight, depth + axisHeight)

        val totalHeight =
            maxOf(
                maxDistFromAxis / 500 * delimiterFactor,
                2 * maxDistFromAxis - delimiterExtend,
            )

        return makeCustomSizedDelim(delim, totalHeight, true, options, mode, classes)
    }

    private const val LAP_IN_EMS = 0.008
    private const val VB_PAD = 80
    private const val EM_PAD = 0.08

    private val lap = VListChild.Kern(-1 * LAP_IN_EMS)
    private val verts = setOf("|", "\\lvert", "\\rvert", "\\vert")
    private val doubleVerts = setOf("\\|", "\\lVert", "\\rVert", "\\Vert")

    private val stackLargeDelimiters =
        setOf(
            "(", "\\lparen", ")", "\\rparen",
            "[", "\\lbrack", "]", "\\rbrack",
            "\\{", "\\lbrace", "\\}", "\\rbrace",
            "\\lfloor", "\\rfloor", "⌊", "⌋",
            "\\lceil", "\\rceil", "⌈", "⌉",
            "\\surd",
        )

    private val stackAlwaysDelimiters =
        setOf(
            "\\uparrow", "\\downarrow", "\\updownarrow",
            "\\Uparrow", "\\Downarrow", "\\Updownarrow",
            "|", "\\|", "\\vert", "\\Vert",
            "\\lvert", "\\rvert", "\\lVert", "\\rVert",
            "\\lgroup", "\\rgroup", "⟮", "⟯",
            "\\lmoustache", "\\rmoustache", "⎰", "⎱",
        )

    private val stackNeverDelimiters =
        setOf(
            "<", ">", "\\langle", "\\rangle", "/", "\\backslash", "\\lt", "\\gt",
        )

    private sealed interface DelimType {
        data class Small(val style: Style) : DelimType

        data class Large(val size: Int) : DelimType

        data object Stack : DelimType
    }

    private val stackNeverDelimiterSequence: List<DelimType> =
        listOf(
            DelimType.Small(Style.SCRIPTSCRIPT),
            DelimType.Small(Style.SCRIPT),
            DelimType.Small(Style.TEXT),
            DelimType.Large(1),
            DelimType.Large(2),
            DelimType.Large(3),
            DelimType.Large(4),
        )

    private val stackAlwaysDelimiterSequence: List<DelimType> =
        listOf(
            DelimType.Small(Style.SCRIPTSCRIPT),
            DelimType.Small(Style.SCRIPT),
            DelimType.Small(Style.TEXT),
            DelimType.Stack,
        )

    private val stackLargeDelimiterSequence: List<DelimType> =
        listOf(
            DelimType.Small(Style.SCRIPTSCRIPT),
            DelimType.Small(Style.SCRIPT),
            DelimType.Small(Style.TEXT),
            DelimType.Large(1),
            DelimType.Large(2),
            DelimType.Large(3),
            DelimType.Large(4),
            DelimType.Stack,
        )

    private fun getMetrics(symbol: String, font: String, mode: Mode): CharacterMetrics {
        val replace = Symbols[Mode.MATH][symbol]?.replace
        val lookup = if (!replace.isNullOrEmpty()) replace else symbol
        return getCharacterMetrics(lookup, font, mode)
            ?: throw IllegalStateException("Unsupported symbol $symbol and font size $font.")
    }

    private fun styleWrap(delim: HtmlDomNode, toStyle: Style, options: Options, classes: List<String>): Span {
        val newOptions = options.havingBaseStyle(toStyle)

        val span = makeSpan(classes + newOptions.sizingClasses(options), listOf(delim), options)

        val delimSizeMultiplier = newOptions.sizeMultiplier / options.sizeMultiplier
        span.height *= delimSizeMultiplier
        span.depth *= delimSizeMultiplier
        span.maxFontSize = newOptions.sizeMultiplier

        return span
    }

    private fun centerSpan(span: Span, options: Options, style: Style) {
        val newOptions = options.havingBaseStyle(style)
        val shift = (1 - options.sizeMultiplier / newOptions.sizeMultiplier) * options.fontMetrics().axisHeight

        span.classes.add("delimcenter")
        span.style.top = makeEm(shift)
        span.height -= shift
        span.depth += shift
    }

    private fun makeSmallDelim(
        delim: String,
        style: Style,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: List<String>,
    ): Span {
        val text = makeSymbol(delim, "Main-Regular", mode, options)
        val span = styleWrap(text, style, options, classes)
        if (center) {
            centerSpan(span, options, style)
        }
        return span
    }

    private fun mathrmSize(value: String, size: Int, mode: Mode, options: Options): SymbolNode =
        makeSymbol(value, "Size$size-Regular", mode, options)

    private fun makeLargeDelim(
        delim: String,
        size: Int,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: List<String>,
    ): Span {
        val inner = mathrmSize(delim, size, mode, options)
        val span =
            styleWrap(
                makeSpan(listOf("delimsizing", "size$size"), listOf(inner), options),
                Style.TEXT,
                options,
                classes,
            )
        if (center) {
            centerSpan(span, options, Style.TEXT)
        }
        return span
    }

    private fun makeGlyphSpan(symbol: String, font: String, mode: Mode, options: Options): VListChild.Elem {
        val sizeClass =
            if (font == "Size1-Regular") {
                "delim-size1"
            } else {
                "delim-size4"
            }

        val corner =
            makeSpan(
                listOf("delimsizinginner", sizeClass),
                listOf(makeSpan(emptyList(), listOf(makeSymbol(symbol, font, mode, options)))),
            )

        return VListChild.Elem(elem = corner)
    }

    private fun makeInner(ch: String, height: Double, options: Options): VListChild.Elem {
        val width =
            (getCharacterMetrics(ch, "Size4-Regular", Mode.MATH) ?: getCharacterMetrics(ch, "Size1-Regular", Mode.MATH)!!).width
        val path = PathNode("inner", SvgGeometry.innerPath(ch, Math.round(1000 * height).toDouble()))
        val svgNode =
            SvgNode(
                listOf(path),
                mutableMapOf(
                    "width" to makeEm(width),
                    "height" to makeEm(height),
                    "style" to "width:" + makeEm(width),
                    "viewBox" to "0 0 " + svgNumber(1000 * width) + " " + svgNumber(Math.round(1000 * height).toDouble()),
                    "preserveAspectRatio" to "xMinYMin",
                ),
            )
        val span = makeSvgSpan(emptyList(), listOf(svgNode), options)
        span.height = height
        span.style.height = makeEm(height)
        span.style.width = makeEm(width)
        return VListChild.Elem(elem = span)
    }

    private fun makeStackedDelim(
        delim: String,
        heightTotal: Double,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: List<String>,
    ): Span {
        var top = delim
        var middle: String? = null
        var repeat = delim
        var bottom = delim
        var svgLabel = ""
        var viewBoxWidth = 0
        var font = "Size1-Regular"

        if (delim == "\\uparrow") {
            repeat = "⏐"
            bottom = "⏐"
        } else if (delim == "\\Uparrow") {
            repeat = "‖"
            bottom = "‖"
        } else if (delim == "\\downarrow") {
            top = "⏐"
            repeat = "⏐"
        } else if (delim == "\\Downarrow") {
            top = "‖"
            repeat = "‖"
        } else if (delim == "\\updownarrow") {
            top = "\\uparrow"
            repeat = "⏐"
            bottom = "\\downarrow"
        } else if (delim == "\\Updownarrow") {
            top = "\\Uparrow"
            repeat = "‖"
            bottom = "\\Downarrow"
        } else if (delim in verts) {
            repeat = "∣"
            svgLabel = "vert"
            viewBoxWidth = 333
        } else if (delim in doubleVerts) {
            repeat = "∥"
            svgLabel = "doublevert"
            viewBoxWidth = 556
        } else if (delim == "[" || delim == "\\lbrack") {
            top = "⎡"
            repeat = "⎢"
            bottom = "⎣"
            font = "Size4-Regular"
            svgLabel = "lbrack"
            viewBoxWidth = 667
        } else if (delim == "]" || delim == "\\rbrack") {
            top = "⎤"
            repeat = "⎥"
            bottom = "⎦"
            font = "Size4-Regular"
            svgLabel = "rbrack"
            viewBoxWidth = 667
        } else if (delim == "\\lfloor" || delim == "⌊") {
            repeat = "⎢"
            top = "⎢"
            bottom = "⎣"
            font = "Size4-Regular"
            svgLabel = "lfloor"
            viewBoxWidth = 667
        } else if (delim == "\\lceil" || delim == "⌈") {
            top = "⎡"
            repeat = "⎢"
            bottom = "⎢"
            font = "Size4-Regular"
            svgLabel = "lceil"
            viewBoxWidth = 667
        } else if (delim == "\\rfloor" || delim == "⌋") {
            repeat = "⎥"
            top = "⎥"
            bottom = "⎦"
            font = "Size4-Regular"
            svgLabel = "rfloor"
            viewBoxWidth = 667
        } else if (delim == "\\rceil" || delim == "⌉") {
            top = "⎤"
            repeat = "⎥"
            bottom = "⎥"
            font = "Size4-Regular"
            svgLabel = "rceil"
            viewBoxWidth = 667
        } else if (delim == "(" || delim == "\\lparen") {
            top = "⎛"
            repeat = "⎜"
            bottom = "⎝"
            font = "Size4-Regular"
            svgLabel = "lparen"
            viewBoxWidth = 875
        } else if (delim == ")" || delim == "\\rparen") {
            top = "⎞"
            repeat = "⎟"
            bottom = "⎠"
            font = "Size4-Regular"
            svgLabel = "rparen"
            viewBoxWidth = 875
        } else if (delim == "\\{" || delim == "\\lbrace") {
            top = "⎧"
            middle = "⎨"
            bottom = "⎩"
            repeat = "⎪"
            font = "Size4-Regular"
        } else if (delim == "\\}" || delim == "\\rbrace") {
            top = "⎫"
            middle = "⎬"
            bottom = "⎭"
            repeat = "⎪"
            font = "Size4-Regular"
        } else if (delim == "\\lgroup" || delim == "⟮") {
            top = "⎧"
            bottom = "⎩"
            repeat = "⎪"
            font = "Size4-Regular"
        } else if (delim == "\\rgroup" || delim == "⟯") {
            top = "⎫"
            bottom = "⎭"
            repeat = "⎪"
            font = "Size4-Regular"
        } else if (delim == "\\lmoustache" || delim == "⎰") {
            top = "⎧"
            bottom = "⎭"
            repeat = "⎪"
            font = "Size4-Regular"
        } else if (delim == "\\rmoustache" || delim == "⎱") {
            top = "⎫"
            bottom = "⎩"
            repeat = "⎪"
            font = "Size4-Regular"
        }

        val topMetrics = getMetrics(top, font, mode)
        val topHeightTotal = topMetrics.height + topMetrics.depth
        val repeatMetrics = getMetrics(repeat, font, mode)
        val repeatHeightTotal = repeatMetrics.height + repeatMetrics.depth
        val bottomMetrics = getMetrics(bottom, font, mode)
        val bottomHeightTotal = bottomMetrics.height + bottomMetrics.depth
        var middleHeightTotal = 0.0
        var middleFactor = 1
        if (middle != null) {
            val middleMetrics = getMetrics(middle, font, mode)
            middleHeightTotal = middleMetrics.height + middleMetrics.depth
            middleFactor = 2
        }

        val minHeight = topHeightTotal + bottomHeightTotal + middleHeightTotal

        val repeatCount = maxOf(0.0, Math.ceil((heightTotal - minHeight) / (middleFactor * repeatHeightTotal)))

        val realHeightTotal = minHeight + repeatCount * middleFactor * repeatHeightTotal

        var axisHeight = options.fontMetrics().axisHeight
        if (center) {
            axisHeight *= options.sizeMultiplier
        }
        val depth = realHeightTotal / 2 - axisHeight

        val stack = mutableListOf<VListChild>()

        if (svgLabel.isNotEmpty()) {
            val midHeight = realHeightTotal - topHeightTotal - bottomHeightTotal
            val viewBoxHeight = Math.round(realHeightTotal * 1000).toDouble()
            val pathStr = SvgGeometry.tallDelim(svgLabel, Math.round(midHeight * 1000).toDouble())
            val path = PathNode(svgLabel, pathStr)
            val width = makeEm(viewBoxWidth / 1000.0)
            val height = makeEm(viewBoxHeight / 1000)
            val svg =
                SvgNode(
                    listOf(path),
                    mutableMapOf(
                        "width" to width,
                        "height" to height,
                        "viewBox" to "0 0 $viewBoxWidth ${svgNumber(viewBoxHeight)}",
                    ),
                )
            val wrapper = makeSvgSpan(emptyList(), listOf(svg), options)
            wrapper.height = viewBoxHeight / 1000
            wrapper.style.width = width
            wrapper.style.height = height
            stack.add(VListChild.Elem(elem = wrapper))
        } else {
            stack.add(makeGlyphSpan(bottom, font, mode, options))
            stack.add(lap)

            if (middle == null) {
                val innerHeight = realHeightTotal - topHeightTotal - bottomHeightTotal + 2 * LAP_IN_EMS
                stack.add(makeInner(repeat, innerHeight, options))
            } else {
                val innerHeight =
                    (realHeightTotal - topHeightTotal - bottomHeightTotal - middleHeightTotal) / 2 + 2 * LAP_IN_EMS
                stack.add(makeInner(repeat, innerHeight, options))
                stack.add(lap)
                stack.add(makeGlyphSpan(middle, font, mode, options))
                stack.add(lap)
                stack.add(makeInner(repeat, innerHeight, options))
            }

            stack.add(lap)
            stack.add(makeGlyphSpan(top, font, mode, options))
        }

        val newOptions = options.havingBaseStyle(Style.TEXT)
        val inner = makeVList(VListParam.Bottom(depth, stack), newOptions)

        return styleWrap(
            makeSpan(listOf("delimsizing", "mult"), listOf(inner), newOptions),
            Style.TEXT,
            options,
            classes,
        )
    }

    private fun sqrtSvg(
        sqrtName: String,
        height: Double,
        viewBoxHeight: Double,
        extraVinculum: Double,
        options: Options,
    ): Span {
        val path = SvgGeometry.sqrtPath(sqrtName, extraVinculum, viewBoxHeight)
        val pathNode = PathNode(sqrtName, path)

        val svg =
            SvgNode(
                listOf(pathNode),
                mutableMapOf(
                    "width" to "400em",
                    "height" to makeEm(height),
                    "viewBox" to "0 0 400000 " + svgNumber(viewBoxHeight),
                    "preserveAspectRatio" to "xMinYMin slice",
                ),
            )

        return makeSvgSpan(listOf("hide-tail"), listOf(svg), options)
    }

    private fun delimTypeToFont(type: DelimType): String =
        when (type) {
            is DelimType.Small -> "Main-Regular"
            is DelimType.Large -> "Size" + type.size + "-Regular"
            DelimType.Stack -> "Size4-Regular"
        }

    private fun traverseSequence(delim: String, height: Double, sequence: List<DelimType>, options: Options): DelimType {
        val start = minOf(2, 3 - options.style.size)
        for (i in start until sequence.size) {
            val delimType = sequence[i]
            if (delimType == DelimType.Stack) {
                break
            }

            val metrics = getMetrics(delim, delimTypeToFont(delimType), Mode.MATH)
            var heightDepth = metrics.height + metrics.depth

            if (delimType is DelimType.Small) {
                val newOptions = options.havingBaseStyle(delimType.style)
                heightDepth *= newOptions.sizeMultiplier
            }

            if (heightDepth > height) {
                return delimType
            }
        }

        return sequence[sequence.size - 1]
    }
}
