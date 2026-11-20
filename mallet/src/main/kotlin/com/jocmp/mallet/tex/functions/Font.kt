package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.normalizeArgument

private val fontAliases =
    mapOf(
        "\\Bbb" to "\\mathbb",
        "\\bold" to "\\mathbf",
        "\\frak" to "\\mathfrak",
    )

internal fun defineFont() {
    defineFunction(
        type = "font",
        names =
            listOf(
                "\\mathrm", "\\mathit", "\\mathbf", "\\mathnormal", "\\mathsfit",
                "\\mathbb", "\\mathcal", "\\mathfrak", "\\mathscr", "\\mathsf",
                "\\mathtt",
                "\\Bbb", "\\bold", "\\frak",
            ),
        numArgs = 1,
        allowedInArgument = true,
        handler = { context, args, _ ->
            val body = normalizeArgument(args[0])
            val func = fontAliases[context.funcName] ?: context.funcName
            ParseNode.Font(
                mode = context.parser.mode,
                font = func.substring(1),
                body = body,
            )
        },
        htmlBuilder = ::fontHtmlBuilder,
    )

    defineFunction(
        type = "mclass",
        names = listOf("\\boldsymbol", "\\bm"),
        numArgs = 1,
        handler = { context, args, _ ->
            val parser = context.parser
            val body = args[0]
            ParseNode.MClass(
                mode = parser.mode,
                mclass = binrelClass(body),
                body =
                    listOf(
                        ParseNode.Font(
                            mode = parser.mode,
                            font = "boldsymbol",
                            body = body,
                        ),
                    ),
                isCharacterBox = isCharacterBox(body),
            )
        },
    )

    defineFunction(
        type = "font",
        names = listOf("\\rm", "\\sf", "\\tt", "\\bf", "\\it", "\\cal"),
        numArgs = 0,
        allowedInText = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val mode = parser.mode
            val body = parser.parseExpression(true, context.breakOnTokenText)

            ParseNode.Font(
                mode = mode,
                font = "math${context.funcName.substring(1)}",
                body = ParseNode.OrdGroup(mode = parser.mode, body = body),
            )
        },
    )
}

private fun fontHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    val node = assertNodeType<ParseNode.Font>(group)
    val font = node.font
    val newOptions = options.withFont(font)
    return buildGroup(node.body, newOptions)
}
