package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.MacroString
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.ordargument

internal fun defineColor() {
    defineFunction(
        type = "color",
        names = listOf("\\textcolor"),
        numArgs = 2,
        allowedInText = true,
        argTypes = listOf(ArgType.COLOR, ArgType.ORIGINAL),
        handler = { context, args, _ ->
            val color = assertNodeType<ParseNode.ColorToken>(args[0]).color
            val body = args[1]
            ParseNode.Color(
                mode = context.parser.mode,
                color = color,
                body = ordargument(body),
            )
        },
        htmlBuilder = ::colorHtmlBuilder,
    )

    defineFunction(
        type = "color",
        names = listOf("\\color"),
        numArgs = 1,
        allowedInText = true,
        argTypes = listOf(ArgType.COLOR),
        handler = { context, args, _ ->
            val parser = context.parser
            val color = assertNodeType<ParseNode.ColorToken>(args[0]).color

            parser.gullet.macros.set("\\current@color", MacroString(color))

            val body = parser.parseExpression(true, context.breakOnTokenText)

            ParseNode.Color(
                mode = parser.mode,
                color = color,
                body = body,
            )
        },
    )
}

private fun colorHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    val node = assertNodeType<ParseNode.Color>(group)
    val elements = buildExpression(node.body, options.withColor(node.color), false)

    return makeFragment(elements)
}
