package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.ordargument

internal fun defineHBox() {
    defineFunction(
        type = "hbox",
        names = listOf("\\hbox"),
        numArgs = 1,
        argTypes = listOf(ArgType.TEXT),
        allowedInText = true,
        primitive = true,
        handler = { context, args, _ ->
            ParseNode.HBox(
                mode = context.parser.mode,
                body = ordargument(args[0]),
            )
        },
        htmlBuilder = { group, options ->
            val hbox = assertNodeType<ParseNode.HBox>(group)
            val elements = buildExpression(hbox.body, options.withFont(""), false)
            makeFragment(elements)
        },
    )
}
