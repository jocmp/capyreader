package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.SymbolNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeSymbol
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.mathsym
import com.jocmp.mallet.tex.ordargument
import com.jocmp.mallet.tex.staticSvg

private val noSuccessor = setOf("\\smallint")

private val singleCharBigOps =
    mapOf(
        "∏" to "\\prod",
        "∐" to "\\coprod",
        "∑" to "\\sum",
        "⋀" to "\\bigwedge",
        "⋁" to "\\bigvee",
        "⋂" to "\\bigcap",
        "⋃" to "\\bigcup",
        "⨀" to "\\bigodot",
        "⨁" to "\\bigoplus",
        "⨂" to "\\bigotimes",
        "⨄" to "\\biguplus",
        "⨆" to "\\bigsqcup",
    )

private val singleCharIntegrals =
    mapOf(
        "∫" to "\\int",
        "∬" to "\\iint",
        "∭" to "\\iiint",
        "∮" to "\\oint",
        "∯" to "\\oiint",
        "∰" to "\\oiiint",
    )

internal fun defineOp() {
    defineFunction(
        type = "op",
        names =
            listOf(
                "\\coprod", "\\bigvee", "\\bigwedge", "\\biguplus", "\\bigcap",
                "\\bigcup", "\\intop", "\\prod", "\\sum", "\\bigotimes",
                "\\bigoplus", "\\bigodot", "\\bigsqcup", "\\smallint", "∏",
                "∐", "∑", "⋀", "⋁", "⋂", "⋃", "⨀",
                "⨁", "⨂", "⨄", "⨆",
            ),
        numArgs = 0,
        handler = { context, _, _ ->
            var fName: String? = context.funcName
            if (fName?.length == 1) {
                fName = singleCharBigOps[fName]
            }
            ParseNode.Op(
                mode = context.parser.mode,
                limits = true,
                parentIsSupSub = false,
                symbol = true,
                name = fName,
            )
        },
        htmlBuilder = ::opHtmlBuilder,
    )

    defineFunction(
        type = "op",
        names = listOf("\\mathop"),
        numArgs = 1,
        primitive = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Op(
                mode = context.parser.mode,
                limits = false,
                parentIsSupSub = false,
                symbol = false,
                body = ordargument(body),
            )
        },
    )

    defineFunction(
        type = "op",
        names =
            listOf(
                "\\arcsin", "\\arccos", "\\arctan", "\\arctg", "\\arcctg",
                "\\arg", "\\ch", "\\cos", "\\cosec", "\\cosh", "\\cot", "\\cotg",
                "\\coth", "\\csc", "\\ctg", "\\cth", "\\deg", "\\dim", "\\exp",
                "\\hom", "\\ker", "\\lg", "\\ln", "\\log", "\\sec", "\\sin",
                "\\sinh", "\\sh", "\\tan", "\\tanh", "\\tg", "\\th",
            ),
        numArgs = 0,
        handler = { context, _, _ ->
            ParseNode.Op(
                mode = context.parser.mode,
                limits = false,
                parentIsSupSub = false,
                symbol = false,
                name = context.funcName,
            )
        },
    )

    defineFunction(
        type = "op",
        names = listOf("\\det", "\\gcd", "\\inf", "\\lim", "\\max", "\\min", "\\Pr", "\\sup"),
        numArgs = 0,
        handler = { context, _, _ ->
            ParseNode.Op(
                mode = context.parser.mode,
                limits = true,
                parentIsSupSub = false,
                symbol = false,
                name = context.funcName,
            )
        },
    )

    defineFunction(
        type = "op",
        names =
            listOf(
                "\\int", "\\iint", "\\iiint", "\\oint", "\\oiint", "\\oiiint",
                "∫", "∬", "∭", "∮", "∯", "∰",
            ),
        numArgs = 0,
        allowedInArgument = true,
        handler = { context, _, _ ->
            var fName: String? = context.funcName
            if (fName?.length == 1) {
                fName = singleCharIntegrals[fName]
            }
            ParseNode.Op(
                mode = context.parser.mode,
                limits = false,
                parentIsSupSub = false,
                symbol = true,
                name = fName,
            )
        },
    )
}

/** Builds an op, or a supsub whose base is an op with limits (\int etc. affect scripting). */
internal fun opHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    var supGroup: ParseNode? = null
    var subGroup: ParseNode? = null
    var hasLimits = false
    val op: ParseNode.Op
    if (group is ParseNode.SupSub) {
        supGroup = group.sup
        subGroup = group.sub
        op = assertNodeType<ParseNode.Op>(group.base)
        hasLimits = true
    } else {
        op = assertNodeType<ParseNode.Op>(group)
    }

    val style = options.style

    var large = false
    if (style.size == Style.DISPLAY.size && op.symbol && op.name !in noSuccessor) {
        large = true
    }

    var base: HtmlDomNode
    if (op.symbol) {
        val fontName = if (large) "Size2-Regular" else "Size1-Regular"

        // KaTeX swaps group.name for \iint/\iiint here and restores it below; a local keeps Op.name immutable.
        var symbolName = op.name.orEmpty()
        var stash = ""
        if (op.name == "\\oiint" || op.name == "\\oiiint") {
            stash = op.name.substring(1)
            symbolName = if (stash == "oiint") "\\iint" else "\\iiint"
        }

        val symbol =
            makeSymbol(
                symbolName, fontName, Mode.MATH, options,
                listOf("mop", "op-symbol", if (large) "large-op" else "small-op"),
            )
        base = symbol
        val symbolItalic = symbol.italic

        if (stash.isNotEmpty()) {
            val oval = staticSvg(stash + "Size" + (if (large) "2" else "1"), options)
            val vlist =
                makeVList(
                    VListParam.IndividualShift(
                        listOf(
                            VListChild.Elem(elem = symbol, shift = 0.0),
                            VListChild.Elem(elem = oval, shift = if (large) 0.08 else 0.0),
                        ),
                    ),
                    options,
                )
            vlist.classes.add(0, "mop")
            vlist.italic = symbolItalic
            base = vlist
        }
    } else if (op.body != null) {
        val inner = buildExpression(op.body, options, true)
        val first = inner.singleOrNull()
        if (first is SymbolNode) {
            base = first
            if (base.classes.isEmpty()) {
                base.classes.add("mop")
            } else {
                base.classes[0] = "mop"
            }
        } else {
            base = makeSpan(listOf("mop"), inner, options)
        }
    } else {
        val name = op.name!!
        val output = (1 until name.length).map { i -> mathsym(name[i].toString(), op.mode, options) }
        base = makeSpan(listOf("mop"), output, options)
    }

    var baseShift = 0.0
    var slant = 0.0
    if ((base is SymbolNode || op.name == "\\oiint" || op.name == "\\oiiint") && op.suppressBaseShift != true) {
        baseShift = (base.height - base.depth) / 2 - options.fontMetrics().axisHeight

        slant =
            when (base) {
                is SymbolNode -> base.italic
                is Span -> base.italic ?: 0.0
                else -> 0.0
            }
    }

    if (hasLimits) {
        return assembleSupSub(base, supGroup, subGroup, options, style, slant, baseShift)
    } else {
        if (baseShift != 0.0) {
            base.style.position = "relative"
            base.style.top = makeEm(baseShift)
        }

        return base
    }
}
