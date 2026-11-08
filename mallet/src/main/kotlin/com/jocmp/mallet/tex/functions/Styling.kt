package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.styleMap

internal fun defineStyling() {
    defineFunction(
        type = "styling",
        names = listOf("\\displaystyle", "\\textstyle", "\\scriptstyle", "\\scriptscriptstyle"),
        numArgs = 0,
        allowedInText = true,
        primitive = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val funcName = context.funcName
            val body = parser.parseExpression(true, context.breakOnTokenText)

            val style = funcName.substring(1, funcName.length - 5)
            if (style !in styleMap) {
                throw IllegalStateException("Unknown style: $style")
            }
            ParseNode.Styling(
                mode = parser.mode,
                style = style,
                body = body,
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Styling>(group)
            val newStyle = styleMap.getValue(node.style)
            var newOptions = options.havingStyle(newStyle)
            if (node.resetFont == true) {
                newOptions = newOptions.withFont("")
            }
            sizingGroup(node.body, newOptions, options)
        },
    )
}
