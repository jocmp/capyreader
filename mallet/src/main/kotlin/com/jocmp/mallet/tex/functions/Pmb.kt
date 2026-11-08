package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

internal fun definePmb() {
    defineFunction(
        type = "pmb",
        names = listOf("\\pmb"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            ParseNode.Pmb(
                mode = context.parser.mode,
                mclass = binrelClass(args[0]),
                body = ordargument(args[0]),
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.Pmb>(node)
            val elements = buildExpression(group.body, options, true)
            val span = makeSpan(listOf(group.mclass), elements, options)
            span.style.textShadow = "0.02em 0.01em 0.04px"
            span
        },
    )
}
