package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan

internal fun defineCr() {
    defineFunction(
        type = "cr",
        names = listOf("\\\\"),
        numArgs = 0,
        numOptionalArgs = 0,
        allowedInText = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val size = if (parser.gullet.future().text == "[") parser.parseSizeGroup(true) else null
            val newLine =
                !parser.settings.displayMode ||
                    !parser.settings.useStrictBehavior(
                        "newLineInDisplayMode",
                        "In LaTeX, \\\\ or \\newline does nothing in display mode",
                    )
            ParseNode.Cr(
                mode = parser.mode,
                newLine = newLine,
                size = size?.let { assertNodeType<ParseNode.Size>(it).value },
            )
        },
        htmlBuilder = { group, options ->
            val group = assertNodeType<ParseNode.Cr>(group)
            val span = makeSpan(listOf("mspace"), emptyList(), options)
            if (group.newLine) {
                span.classes.add("katex-newline")
                if (group.size != null) {
                    span.style.marginTop = makeEm(calculateSize(group.size, options))
                }
            }
            span
        },
    )
}
