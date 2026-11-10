package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Delimiter
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Measurement
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeLineSpan
import com.jocmp.mallet.tex.makeNullDelimiter
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.normalizeArgument

private val stylArray = listOf("display", "text", "script", "scriptscript")

internal fun defineGenFrac() {
    defineFunction(
        type = "genfrac",
        names =
            listOf(
                "\\cfrac", "\\dfrac", "\\frac", "\\tfrac",
                "\\dbinom", "\\binom", "\\tbinom",
                "\\\\atopfrac",
                "\\\\bracefrac", "\\\\brackfrac",
            ),
        numArgs = 2,
        allowedInArgument = true,
        handler = { context, args, _ ->
            val funcName = context.funcName
            val numer = args[0]
            val denom = args[1]
            val hasBarLine: Boolean
            var leftDelim: String? = null
            var rightDelim: String? = null

            when (funcName) {
                "\\cfrac", "\\dfrac", "\\frac", "\\tfrac" -> hasBarLine = true
                "\\\\atopfrac" -> hasBarLine = false
                "\\dbinom", "\\binom", "\\tbinom" -> {
                    hasBarLine = false
                    leftDelim = "("
                    rightDelim = ")"
                }
                "\\\\bracefrac" -> {
                    hasBarLine = false
                    leftDelim = "\\{"
                    rightDelim = "\\}"
                }
                "\\\\brackfrac" -> {
                    hasBarLine = false
                    leftDelim = "["
                    rightDelim = "]"
                }
                else -> throw IllegalStateException("Unrecognized genfrac command")
            }

            val continued = funcName == "\\cfrac"
            var style: String? = null
            if (continued || funcName.startsWith("\\d")) {
                style = "display"
            } else if (funcName.startsWith("\\t")) {
                style = "text"
            }

            wrapWithStyle(
                ParseNode.GenFrac(
                    mode = context.parser.mode,
                    continued = continued,
                    numer = numer,
                    denom = denom,
                    hasBarLine = hasBarLine,
                    leftDelim = leftDelim,
                    rightDelim = rightDelim,
                    barSize = null,
                ),
                style,
            )
        },
        htmlBuilder = ::genFracHtmlBuilder,
    )

    defineFunction(
        type = "infix",
        names = listOf("\\over", "\\choose", "\\atop", "\\brace", "\\brack"),
        numArgs = 0,
        infix = true,
        handler = { context, _, _ ->
            val replaceWith =
                when (context.funcName) {
                    "\\over" -> "\\frac"
                    "\\choose" -> "\\binom"
                    "\\atop" -> "\\\\atopfrac"
                    "\\brace" -> "\\\\bracefrac"
                    "\\brack" -> "\\\\brackfrac"
                    else -> throw IllegalStateException("Unrecognized infix genfrac command")
                }
            ParseNode.Infix(
                mode = context.parser.mode,
                replaceWith = replaceWith,
                token = context.token,
            )
        },
    )

    defineFunction(
        type = "genfrac",
        names = listOf("\\genfrac"),
        numArgs = 6,
        allowedInArgument = true,
        argTypes = listOf(ArgType.MATH, ArgType.MATH, ArgType.SIZE, ArgType.TEXT, ArgType.MATH, ArgType.MATH),
        handler = { context, args, _ ->
            val numer = args[4]
            val denom = args[5]

            val leftNode = normalizeArgument(args[0])
            val leftDelim =
                if (leftNode is ParseNode.Atom && leftNode.family == "open") delimFromValue(leftNode.text) else null
            val rightNode = normalizeArgument(args[1])
            val rightDelim =
                if (rightNode is ParseNode.Atom && rightNode.family == "close") delimFromValue(rightNode.text) else null

            val barNode = assertNodeType<ParseNode.Size>(args[2])
            val hasBarLine: Boolean
            var barSize: Measurement? = null
            if (barNode.isBlank) {
                hasBarLine = true
            } else {
                barSize = barNode.value
                hasBarLine = barSize.number > 0
            }

            var size: String? = null
            val styl = args[3]
            if (styl is ParseNode.OrdGroup) {
                if (styl.body.isNotEmpty()) {
                    val textOrd = assertNodeType<ParseNode.TextOrd>(styl.body[0])
                    size = styleAt(textOrd.text)
                }
            } else {
                val textOrd = assertNodeType<ParseNode.TextOrd>(styl)
                size = styleAt(textOrd.text)
            }

            wrapWithStyle(
                ParseNode.GenFrac(
                    mode = context.parser.mode,
                    continued = false,
                    numer = numer,
                    denom = denom,
                    hasBarLine = hasBarLine,
                    leftDelim = leftDelim,
                    rightDelim = rightDelim,
                    barSize = barSize,
                ),
                size,
            )
        },
    )

    defineFunction(
        type = "infix",
        names = listOf("\\above"),
        numArgs = 1,
        argTypes = listOf(ArgType.SIZE),
        infix = true,
        handler = { context, args, _ ->
            ParseNode.Infix(
                mode = context.parser.mode,
                replaceWith = "\\\\abovefrac",
                size = assertNodeType<ParseNode.Size>(args[0]).value,
                token = context.token,
            )
        },
    )

    defineFunction(
        type = "genfrac",
        names = listOf("\\\\abovefrac"),
        numArgs = 3,
        argTypes = listOf(ArgType.MATH, ArgType.SIZE, ArgType.MATH),
        handler = { context, args, _ ->
            val numer = args[0]
            val barSize =
                assertNodeType<ParseNode.Infix>(args[1]).size
                    ?: throw IllegalStateException("\\\\abovefrac expected size, but got null")

            val denom = args[2]

            val hasBarLine = barSize.number > 0
            ParseNode.GenFrac(
                mode = context.parser.mode,
                continued = false,
                numer = numer,
                denom = denom,
                hasBarLine = hasBarLine,
                leftDelim = null,
                rightDelim = null,
                barSize = barSize,
            )
        },
    )
}

private fun genFracHtmlBuilder(node: ParseNode, options: Options): HtmlDomNode {
    val group = assertNodeType<ParseNode.GenFrac>(node)
    val style = options.style

    val nstyle = style.fracNum()
    val dstyle = style.fracDen()
    var newOptions: Options

    newOptions = options.havingStyle(nstyle)
    val numerm = buildGroup(group.numer, newOptions, options)

    if (group.continued) {
        val hStrut = 8.5 / options.fontMetrics().ptPerEm
        val dStrut = 3.5 / options.fontMetrics().ptPerEm
        numerm.height = if (numerm.height < hStrut) hStrut else numerm.height
        numerm.depth = if (numerm.depth < dStrut) dStrut else numerm.depth
    }

    newOptions = options.havingStyle(dstyle)
    val denomm = buildGroup(group.denom, newOptions, options)

    val rule: Span?
    val ruleWidth: Double
    val ruleSpacing: Double
    if (group.hasBarLine) {
        val barSize = group.barSize
        rule =
            if (barSize != null) {
                makeLineSpan("frac-line", options, calculateSize(barSize, options))
            } else {
                makeLineSpan("frac-line", options)
            }
        ruleWidth = rule.height
        ruleSpacing = rule.height
    } else {
        rule = null
        ruleWidth = 0.0
        ruleSpacing = options.fontMetrics().defaultRuleThickness
    }

    var numShift: Double
    val clearance: Double
    var denomShift: Double
    if (style.size == Style.DISPLAY.size) {
        numShift = options.fontMetrics().num1
        clearance =
            if (ruleWidth > 0) {
                3 * ruleSpacing
            } else {
                7 * ruleSpacing
            }
        denomShift = options.fontMetrics().denom1
    } else {
        if (ruleWidth > 0) {
            numShift = options.fontMetrics().num2
            clearance = ruleSpacing
        } else {
            numShift = options.fontMetrics().num3
            clearance = 3 * ruleSpacing
        }
        denomShift = options.fontMetrics().denom2
    }

    val frac =
        if (rule == null) {
            val candidateClearance = (numShift - numerm.depth) - (denomm.height - denomShift)
            if (candidateClearance < clearance) {
                numShift += 0.5 * (clearance - candidateClearance)
                denomShift += 0.5 * (clearance - candidateClearance)
            }

            makeVList(
                VListParam.IndividualShift(
                    listOf(
                        VListChild.Elem(elem = denomm, shift = denomShift),
                        VListChild.Elem(elem = numerm, shift = -numShift),
                    ),
                ),
                options,
            )
        } else {
            val axisHeight = options.fontMetrics().axisHeight

            if ((numShift - numerm.depth) - (axisHeight + 0.5 * ruleWidth) < clearance) {
                numShift += clearance - ((numShift - numerm.depth) - (axisHeight + 0.5 * ruleWidth))
            }

            if ((axisHeight - 0.5 * ruleWidth) - (denomm.height - denomShift) < clearance) {
                denomShift += clearance - ((axisHeight - 0.5 * ruleWidth) - (denomm.height - denomShift))
            }

            val midShift = -(axisHeight - 0.5 * ruleWidth)

            makeVList(
                VListParam.IndividualShift(
                    listOf(
                        VListChild.Elem(elem = denomm, shift = denomShift),
                        VListChild.Elem(elem = rule, shift = midShift),
                        VListChild.Elem(elem = numerm, shift = -numShift),
                    ),
                ),
                options,
            )
        }

    newOptions = options.havingStyle(style)
    frac.height *= newOptions.sizeMultiplier / options.sizeMultiplier
    frac.depth *= newOptions.sizeMultiplier / options.sizeMultiplier

    val delimSize =
        if (style.size == Style.DISPLAY.size) {
            options.fontMetrics().delim1
        } else if (style.size == Style.SCRIPTSCRIPT.size) {
            options.havingStyle(Style.SCRIPT).fontMetrics().delim2
        } else {
            options.fontMetrics().delim2
        }

    val leftDelim =
        if (group.leftDelim == null) {
            makeNullDelimiter(options, listOf("mopen"))
        } else {
            Delimiter.makeCustomSizedDelim(
                group.leftDelim, delimSize, true,
                options.havingStyle(style), group.mode, listOf("mopen"),
            )
        }

    val rightDelim =
        if (group.continued) {
            makeSpan(emptyList())
        } else if (group.rightDelim == null) {
            makeNullDelimiter(options, listOf("mclose"))
        } else {
            Delimiter.makeCustomSizedDelim(
                group.rightDelim, delimSize, true,
                options.havingStyle(style), group.mode, listOf("mclose"),
            )
        }

    return makeSpan(
        listOf("mord") + newOptions.sizingClasses(options),
        listOf(leftDelim, makeSpan(listOf("mfrac"), listOf(frac)), rightDelim),
        options,
    )
}

private fun wrapWithStyle(frac: ParseNode.GenFrac, style: String?): ParseNode {
    if (style.isNullOrEmpty()) {
        return frac
    }

    return ParseNode.Styling(
        mode = frac.mode,
        style = style,
        body = listOf(frac),
    )
}

private fun delimFromValue(delimString: String): String? {
    var delim: String? = null
    if (delimString.isNotEmpty()) {
        delim = delimString
        delim = if (delim == ".") null else delim
    }
    return delim
}

/** JS `stylArray[Number(text)]`: undefined for anything but an integer index in range. */
private fun styleAt(text: String): String? {
    val trimmed = text.trim()
    val number = if (trimmed.isEmpty()) 0.0 else trimmed.toDoubleOrNull() ?: return null
    if (number != Math.floor(number) || number.isInfinite()) {
        return null
    }
    return stylArray.getOrNull(number.toInt())
}
