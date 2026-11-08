package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

internal fun definePhantom() {
    defineFunction(
        type = "phantom",
        names = listOf("\\phantom"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Phantom(
                mode = context.parser.mode,
                body = ordargument(body),
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Phantom>(group)
            val elements = buildExpression(node.body, options.withPhantom(), false)

            makeFragment(elements)
        },
    )

    defineFunction(
        type = "vphantom",
        names = listOf("\\vphantom"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.VPhantom(
                mode = context.parser.mode,
                body = body,
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.VPhantom>(group)
            val inner = makeSpan(listOf("katex-inner"), listOf(buildGroup(node.body, options.withPhantom())))
            val fix = makeSpan(listOf("katex-fix"), emptyList())
            makeSpan(listOf("mord", "rlap"), listOf(inner, fix), options)
        },
    )
}
