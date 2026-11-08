package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeSymbol
import com.jocmp.mallet.tex.tryCombineChars

internal fun defineVerb() {
    defineFunction(
        type = "verb",
        names = listOf("\\verb"),
        numArgs = 0,
        allowedInText = true,
        handler = { _, _, _ ->
            throw ParseError("\\verb ended by end of line instead of matching delimiter")
        },
        htmlBuilder = { group, options ->
            val verb = assertNodeType<ParseNode.Verb>(group)
            val text = makeVerb(verb)
            val body = mutableListOf<HtmlDomNode>()
            val newOptions = options.havingStyle(options.style.text())
            text.forEach { char ->
                var c = char.toString()
                if (c == "~") {
                    c = "\\textasciitilde"
                }
                body.add(makeSymbol(c, "Typewriter-Regular", verb.mode, newOptions, listOf("mord", "texttt")))
            }
            makeSpan(
                listOf("mord", "text") + newOptions.sizingClasses(options),
                tryCombineChars(body),
                newOptions,
            )
        },
    )
}

private fun makeVerb(group: ParseNode.Verb): String = group.body.replace(" ", if (group.star) "␣" else " ")
