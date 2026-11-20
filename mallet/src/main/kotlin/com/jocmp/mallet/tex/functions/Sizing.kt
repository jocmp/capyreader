package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.DocumentFragment
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment

private val sizeFuncs =
    listOf(
        "\\tiny", "\\sixptsize", "\\scriptsize", "\\footnotesize", "\\small",
        "\\normalsize", "\\large", "\\Large", "\\LARGE", "\\huge", "\\Huge",
    )

internal fun sizingGroup(value: List<ParseNode>, options: Options, baseOptions: Options): DocumentFragment {
    val inner = buildExpression(value, options, false)
    val multiplier = options.sizeMultiplier / baseOptions.sizeMultiplier

    inner.forEach { node ->
        val pos = node.classes.indexOf("katex-sizing")
        if (pos < 0) {
            node.classes.addAll(options.sizingClasses(baseOptions))
        } else if (node.classes.getOrNull(pos + 1) == "reset-size" + options.size) {
            node.classes[pos + 1] = "reset-size" + baseOptions.size
        }

        node.height *= multiplier
        node.depth *= multiplier
    }

    return makeFragment(inner)
}

internal fun defineSizing() {
    defineFunction(
        type = "sizing",
        names = sizeFuncs,
        numArgs = 0,
        allowedInText = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val body = parser.parseExpression(false, context.breakOnTokenText)

            ParseNode.Sizing(
                mode = parser.mode,
                size = sizeFuncs.indexOf(context.funcName) + 1,
                body = body,
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Sizing>(group)
            val newOptions = options.havingSize(node.size)
            sizingGroup(node.body, newOptions, options)
        },
    )
}
