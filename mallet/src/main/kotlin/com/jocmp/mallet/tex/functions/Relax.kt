package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.defineFunction

internal fun defineRelax() {
    defineFunction(
        type = "internal",
        names = listOf("\\relax"),
        numArgs = 0,
        allowedInText = true,
        allowedInArgument = true,
        handler = { context, _, _ ->
            ParseNode.Internal(mode = context.parser.mode)
        },
    )
}
