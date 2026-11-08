package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.ordargument

internal fun defineMathChoice() {
    defineFunction(
        type = "mathchoice",
        names = listOf("\\mathchoice"),
        numArgs = 4,
        primitive = true,
        handler = { context, args, _ ->
            ParseNode.MathChoice(
                mode = context.parser.mode,
                display = ordargument(args[0]),
                text = ordargument(args[1]),
                script = ordargument(args[2]),
                scriptscript = ordargument(args[3]),
            )
        },
        htmlBuilder = { group, options ->
            val body = chooseMathStyle(assertNodeType(group), options)
            val elements = buildExpression(body, options, false)
            makeFragment(elements)
        },
    )
}

private fun chooseMathStyle(group: ParseNode.MathChoice, options: Options): List<ParseNode> =
    when (options.style.size) {
        Style.DISPLAY.size -> group.display
        Style.TEXT.size -> group.text
        Style.SCRIPT.size -> group.script
        Style.SCRIPTSCRIPT.size -> group.scriptscript
        else -> group.text
    }
