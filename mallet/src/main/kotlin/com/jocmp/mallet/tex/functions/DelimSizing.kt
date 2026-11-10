package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Delimiter
import com.jocmp.mallet.tex.FunctionContext
import com.jocmp.mallet.tex.Functions
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.MacroString
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.checkSymbolNodeType
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeNullDelimiter
import com.jocmp.mallet.tex.MiddleDelim
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.normalizeArgument

internal fun defineDelimSizing() {
    defineFunction(
        type = "delimsizing",
        names =
            listOf(
                "\\bigl", "\\Bigl", "\\biggl", "\\Biggl",
                "\\bigr", "\\Bigr", "\\biggr", "\\Biggr",
                "\\bigm", "\\Bigm", "\\biggm", "\\Biggm",
                "\\big", "\\Big", "\\bigg", "\\Bigg",
            ),
        numArgs = 1,
        argTypes = listOf(ArgType.PRIMITIVE),
        handler = { context, args, _ ->
            val delim = checkDelimiter(normalizeArgument(args[0]), context)
            val size = delimiterSizes.getValue(context.funcName)

            ParseNode.DelimSizing(
                mode = context.parser.mode,
                size = size.size,
                mclass = size.mclass,
                delim = delim.text,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.DelimSizing>(node)
            if (group.delim == ".") {
                makeSpan(listOf(group.mclass))
            } else {
                Delimiter.makeSizedDelim(group.delim, group.size, options, group.mode, listOf(group.mclass))
            }
        },
    )

    defineFunction(
        type = "leftright-right",
        names = listOf("\\right"),
        numArgs = 1,
        primitive = true,
        handler = { context, args, _ ->
            val color =
                when (val macro = context.parser.gullet.macros.get("\\current@color")) {
                    null -> null
                    is MacroString -> macro.text
                    else -> throw ParseError("\\current@color set to non-string in \\right")
                }
            ParseNode.LeftRightRight(
                mode = context.parser.mode,
                delim = checkDelimiter(args[0], context).text,
                color = color,
            )
        },
    )

    defineFunction(
        type = "leftright",
        names = listOf("\\left"),
        numArgs = 1,
        primitive = true,
        handler = { context, args, _ ->
            val delim = checkDelimiter(args[0], context)

            val parser = context.parser
            parser.leftrightDepth += 1
            val body = parser.parseExpression(false)
            parser.leftrightDepth -= 1
            parser.expect("\\right", false)
            val right = assertNodeType<ParseNode.LeftRightRight>(parser.parseFunction())
            ParseNode.LeftRight(
                mode = parser.mode,
                body = body,
                left = delim.text,
                right = right.delim,
                rightColor = right.color,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.LeftRight>(node)
            val inner = buildExpression(group.body, options, true, "mopen" to "mclose")

            var innerHeight = 0.0
            var innerDepth = 0.0
            var hadMiddle = false

            inner.forEach { child ->
                if ((child as? Span)?.isMiddle != null) {
                    hadMiddle = true
                } else {
                    innerHeight = maxOf(child.height, innerHeight)
                    innerDepth = maxOf(child.depth, innerDepth)
                }
            }

            innerHeight *= options.sizeMultiplier
            innerDepth *= options.sizeMultiplier

            val leftDelim =
                if (group.left == ".") {
                    makeNullDelimiter(options, listOf("mopen"))
                } else {
                    Delimiter.makeLeftRightDelim(group.left, innerHeight, innerDepth, options, group.mode, listOf("mopen"))
                }
            inner.add(0, leftDelim)

            if (hadMiddle) {
                (1 until inner.size).forEach { i ->
                    val isMiddle = (inner[i] as? Span)?.isMiddle
                    if (isMiddle != null) {
                        inner[i] =
                            Delimiter.makeLeftRightDelim(
                                isMiddle.delim,
                                innerHeight,
                                innerDepth,
                                isMiddle.options,
                                group.mode,
                                emptyList(),
                            )
                    }
                }
            }

            val rightDelim =
                if (group.right == ".") {
                    makeNullDelimiter(options, listOf("mclose"))
                } else {
                    val colorOptions = if (!group.rightColor.isNullOrEmpty()) options.withColor(group.rightColor) else options
                    Delimiter.makeLeftRightDelim(group.right, innerHeight, innerDepth, colorOptions, group.mode, listOf("mclose"))
                }
            inner.add(rightDelim)

            makeSpan(listOf("minner"), inner, options)
        },
    )

    defineFunction(
        type = "middle",
        names = listOf("\\middle"),
        numArgs = 1,
        primitive = true,
        handler = { context, args, _ ->
            val delim = checkDelimiter(args[0], context)
            if (context.parser.leftrightDepth == 0) {
                throw ParseError("\\middle without preceding \\left", delim)
            }

            ParseNode.Middle(
                mode = context.parser.mode,
                delim = delim.text,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.Middle>(node)
            if (group.delim == ".") {
                makeNullDelimiter(options, emptyList())
            } else {
                val middleDelim = Delimiter.makeSizedDelim(group.delim, 1, options, group.mode, emptyList())
                middleDelim.isMiddle = MiddleDelim(delim = group.delim, options = options)
                middleDelim
            }
        },
    )
}

private class DelimiterSize(
    val mclass: String,
    val size: Int,
)

private val delimiterSizes =
    mapOf(
        "\\bigl" to DelimiterSize(mclass = "mopen", size = 1),
        "\\Bigl" to DelimiterSize(mclass = "mopen", size = 2),
        "\\biggl" to DelimiterSize(mclass = "mopen", size = 3),
        "\\Biggl" to DelimiterSize(mclass = "mopen", size = 4),
        "\\bigr" to DelimiterSize(mclass = "mclose", size = 1),
        "\\Bigr" to DelimiterSize(mclass = "mclose", size = 2),
        "\\biggr" to DelimiterSize(mclass = "mclose", size = 3),
        "\\Biggr" to DelimiterSize(mclass = "mclose", size = 4),
        "\\bigm" to DelimiterSize(mclass = "mrel", size = 1),
        "\\Bigm" to DelimiterSize(mclass = "mrel", size = 2),
        "\\biggm" to DelimiterSize(mclass = "mrel", size = 3),
        "\\Biggm" to DelimiterSize(mclass = "mrel", size = 4),
        "\\big" to DelimiterSize(mclass = "mord", size = 1),
        "\\Big" to DelimiterSize(mclass = "mord", size = 2),
        "\\bigg" to DelimiterSize(mclass = "mord", size = 3),
        "\\Bigg" to DelimiterSize(mclass = "mord", size = 4),
    )

private val delimiters =
    setOf(
        "(", "\\lparen", ")", "\\rparen",
        "[", "\\lbrack", "]", "\\rbrack",
        "\\{", "\\lbrace", "\\}", "\\rbrace",
        "\\lfloor", "\\rfloor", "⌊", "⌋",
        "\\lceil", "\\rceil", "⌈", "⌉",
        "<", ">", "\\langle", "⟨", "\\rangle", "⟩", "\\lt", "\\gt",
        "\\lvert", "\\rvert", "\\lVert", "\\rVert",
        "\\lgroup", "\\rgroup", "⟮", "⟯",
        "\\lmoustache", "\\rmoustache", "⎰", "⎱",
        "/", "\\backslash",
        "|", "\\vert", "\\|", "\\Vert",
        "\\uparrow", "\\Uparrow",
        "\\downarrow", "\\Downarrow",
        "\\updownarrow", "\\Updownarrow",
        ".",
    )

private fun checkDelimiter(delim: ParseNode, context: FunctionContext): ParseNode.Symbol {
    val symDelim = checkSymbolNodeType(delim)
    if (symDelim != null && symDelim.text in delimiters) {
        return symDelim
    } else if (symDelim != null) {
        throw ParseError("Invalid delimiter '${symDelim.text}' after '${context.funcName}'", delim)
    } else {
        throw ParseError("Invalid delimiter type '${delim.type}'", delim)
    }
}
