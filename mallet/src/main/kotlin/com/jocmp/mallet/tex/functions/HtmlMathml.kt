package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.ordargument

internal fun defineHtmlMathml() {
    defineFunction(
        type = "htmlmathml",
        names = listOf("\\html@mathml"),
        numArgs = 2,
        allowedInArgument = true,
        allowedInText = true,
        handler = { context, args, _ ->
            ParseNode.HtmlMathml(
                mode = context.parser.mode,
                html = ordargument(args[0]),
                mathml = ordargument(args[1]),
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.HtmlMathml>(group)
            val elements = buildExpression(node.html, options, false)
            makeFragment(elements)
        },
    )
}
