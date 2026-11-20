package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunctionBuilders
import com.jocmp.mallet.tex.makeOrd
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.mathsym

private class RegularSpace(
    val className: String? = null,
)

private val cssSpace =
    mapOf(
        "\\nobreak" to "nobreak",
        "\\allowbreak" to "allowbreak",
    )

private val regularSpace =
    mapOf(
        " " to RegularSpace(),
        "\\ " to RegularSpace(),
        "~" to RegularSpace(className = "nobreak"),
        "\\space" to RegularSpace(),
        "\\nobreakspace" to RegularSpace(className = "nobreak"),
    )

internal fun defineSymbolsSpacing() {
    defineFunctionBuilders(type = "spacing") { node, options ->
        val group = assertNodeType<ParseNode.Spacing>(node)
        val regularSpaceItem = regularSpace[group.text]
        val cssSpaceClass = cssSpace[group.text]

        if (regularSpaceItem != null) {
            val className = regularSpaceItem.className.orEmpty()
            if (group.mode == Mode.TEXT) {
                val ord = makeOrd(group, options)
                ord.classes.add(className)
                ord
            } else {
                makeSpan(
                    listOf("mspace", className),
                    listOf(mathsym(group.text, group.mode, options)),
                    options,
                )
            }
        } else if (cssSpaceClass != null) {
            makeSpan(listOf("mspace", cssSpaceClass), emptyList(), options)
        } else {
            throw ParseError("Unknown type of space \"${group.text}\"")
        }
    }
}
