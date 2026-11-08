package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.defineFunction

internal fun defineMath() {
    defineFunction(
        type = "styling",
        names = listOf("\\(", "$"),
        numArgs = 0,
        allowedInText = true,
        allowedInMath = false,
        handler = { context, _, _ ->
            val parser = context.parser
            val outerMode = parser.mode
            parser.switchMode(Mode.MATH)
            val close = if (context.funcName == "\\(") "\\)" else "$"
            val body = parser.parseExpression(false, close)
            parser.expect(close)
            parser.switchMode(outerMode)
            ParseNode.Styling(
                mode = parser.mode,
                style = "text",
                resetFont = true,
                body = body,
            )
        },
    )

    defineFunction(
        type = "text",
        names = listOf("\\)", "\\]"),
        numArgs = 0,
        allowedInText = true,
        allowedInMath = false,
        handler = { context, _, _ ->
            throw ParseError("Mismatched ${context.funcName}")
        },
    )
}
