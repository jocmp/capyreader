package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertCharacterGroup
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction

private val leadingIntegerRegex = Regex("^\\s*[-+]?\\d+")

internal fun defineChar() {
    defineFunction(
        type = "textord",
        names = listOf("\\@char"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            val arg = assertNodeType<ParseNode.OrdGroup>(args[0])
            val number = assertCharacterGroup(arg, "\\@char has non-numeric argument")
            // JS parseInt: leading integer prefix, NaN when there is none.
            val code = leadingIntegerRegex.find(number)?.value?.trim()?.toLongOrNull()
            val text =
                if (code == null) {
                    throw ParseError("\\@char has non-numeric argument $number")
                } else if (code < 0 || code > 0x10ffff) {
                    throw ParseError("\\@char with invalid code point $number")
                } else if (code <= 0xffff) {
                    code.toInt().toChar().toString()
                } else {
                    val astral = code - 0x10000
                    charArrayOf(
                        ((astral shr 10) + 0xd800).toInt().toChar(),
                        ((astral and 0x3ff) + 0xdc00).toInt().toChar(),
                    ).concatToString()
                }
            ParseNode.TextOrd(
                mode = context.parser.mode,
                text = text,
            )
        },
    )
}
