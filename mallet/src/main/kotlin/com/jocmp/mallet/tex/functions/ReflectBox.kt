package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.FunctionContext
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan

internal fun defineReflectBox() {
    defineFunction(
        type = "reflectbox",
        names = listOf("\\reflectbox"),
        numArgs = 1,
        argTypes = listOf(ArgType.HBOX),
        allowedInText = true,
        handler = { context, args, _ -> reflectBoxHandler(context, args) },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.ReflectBox>(node)
            makeSpan(
                listOf("mord", "reflectbox"),
                listOf(buildGroup(group.body, options)),
                options,
            )
        },
    )

    defineFunction(
        type = "reflectbox",
        names = listOf("\\mathreflectbox"),
        numArgs = 1,
        argTypes = listOf(ArgType.MATH),
        handler = { context, args, _ -> reflectBoxHandler(context, args) },
    )
}

private fun reflectBoxHandler(context: FunctionContext, args: List<ParseNode>): ParseNode =
    ParseNode.ReflectBox(
        mode = context.parser.mode,
        body = args[0],
    )
