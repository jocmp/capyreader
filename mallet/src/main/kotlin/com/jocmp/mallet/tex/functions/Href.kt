package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeAnchor
import com.jocmp.mallet.tex.ordargument

internal fun defineHref() {
    defineFunction(
        type = "href",
        names = listOf("\\href"),
        numArgs = 2,
        argTypes = listOf(ArgType.URL, ArgType.ORIGINAL),
        allowedInText = true,
        handler = { context, args, _ ->
            val parser = context.parser
            val body = args[1]
            val href = assertNodeType<ParseNode.Url>(args[0]).url

            if (!parser.settings.isTrusted(command = "\\href", url = href)) {
                parser.formatUnsupportedCmd("\\href")
            } else {
                ParseNode.Href(
                    mode = parser.mode,
                    href = href,
                    body = ordargument(body),
                )
            }
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Href>(group)
            val elements = buildExpression(node.body, options, false)
            makeAnchor(node.href, emptyList(), elements, options)
        },
    )

    defineFunction(
        type = "href",
        names = listOf("\\url"),
        numArgs = 1,
        argTypes = listOf(ArgType.URL),
        allowedInText = true,
        handler = { context, args, _ ->
            val parser = context.parser
            val href = assertNodeType<ParseNode.Url>(args[0]).url

            if (!parser.settings.isTrusted(command = "\\url", url = href)) {
                return@defineFunction parser.formatUnsupportedCmd("\\url")
            }

            val chars =
                href.map { char ->
                    var c = char.toString()
                    if (c == "~") {
                        c = "\\textasciitilde"
                    }
                    ParseNode.TextOrd(mode = Mode.TEXT, text = c)
                }
            val body =
                ParseNode.Text(
                    mode = parser.mode,
                    font = "\\texttt",
                    body = chars,
                )
            ParseNode.Href(
                mode = parser.mode,
                href = href,
                body = ordargument(body),
            )
        },
    )
}
