package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan

internal fun defineLap() {
    defineFunction(
        type = "lap",
        names = listOf("\\mathllap", "\\mathrlap", "\\mathclap"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Lap(
                mode = context.parser.mode,
                alignment = context.funcName.substring(5),
                body = body,
            )
        },
        htmlBuilder = { group, options ->
            val lap = assertNodeType<ParseNode.Lap>(group)
            var inner: Span
            if (lap.alignment == "clap") {
                inner = makeSpan(emptyList(), listOf(buildGroup(lap.body, options)))
                inner = makeSpan(listOf("katex-inner"), listOf(inner), options)
            } else {
                inner = makeSpan(listOf("katex-inner"), listOf(buildGroup(lap.body, options)))
            }
            val fix = makeSpan(listOf("katex-fix"), emptyList())
            var node = makeSpan(listOf(lap.alignment), listOf(inner, fix), options)

            val strut = makeSpan(listOf("katex-strut"))
            strut.style.height = makeEm(node.height + node.depth)
            if (node.depth != 0.0) {
                strut.style.verticalAlign = makeEm(-node.depth)
            }
            node.children.add(0, strut)

            node = makeSpan(listOf("katex-thinbox"), listOf(node), options)
            makeSpan(listOf("mord", "katex-vbox"), listOf(node), options)
        },
    )
}
