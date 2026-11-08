package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

private const val ESCAPED_COMMA = "{,}"

private val whitespaceRegex = Regex("\\s+")

internal fun defineHtml() {
    defineFunction(
        type = "html",
        names = listOf("\\htmlClass", "\\htmlId", "\\htmlStyle", "\\htmlData"),
        numArgs = 2,
        argTypes = listOf(ArgType.RAW, ArgType.ORIGINAL),
        allowedInText = true,
        handler = { context, args, _ ->
            val parser = context.parser
            val funcName = context.funcName
            val value = assertNodeType<ParseNode.Raw>(args[0]).string
            val body = args[1]

            parser.settings.reportNonstrict("htmlExtension", "HTML extension is disabled on strict mode")

            val attributes = linkedMapOf<String, String>()

            when (funcName) {
                "\\htmlClass" -> attributes["class"] = value
                "\\htmlId" -> attributes["id"] = value
                "\\htmlStyle" -> attributes["style"] = value
                "\\htmlData" -> {
                    val data = mutableListOf<String>()
                    var current = ""
                    var i = 0
                    while (i < value.length) {
                        if (value.startsWith(ESCAPED_COMMA, i)) {
                            current += ","
                            i += ESCAPED_COMMA.length - 1
                        } else if (value[i] == ',') {
                            data.add(current)
                            current = ""
                        } else {
                            current += value[i]
                        }
                        i += 1
                    }
                    data.add(current)
                    data.forEach { item ->
                        val firstEquals = item.indexOf("=")
                        if (firstEquals < 0) {
                            throw ParseError("\\htmlData key/value '$item' missing equals sign")
                        }
                        val key = item.substring(0, firstEquals)
                        val itemValue = item.substring(firstEquals + 1)
                        attributes["data-" + key.trim()] = itemValue
                    }
                }
                else -> throw IllegalStateException("Unrecognized html command")
            }

            if (!parser.settings.isTrusted(command = funcName)) {
                parser.formatUnsupportedCmd(funcName)
            } else {
                ParseNode.Html(
                    mode = parser.mode,
                    attributes = attributes,
                    body = ordargument(body),
                )
            }
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Html>(group)
            val elements = buildExpression(node.body, options, false)

            val classes = mutableListOf("enclosing")
            val className = node.attributes["class"]
            if (!className.isNullOrEmpty()) {
                classes.addAll(className.trim().split(whitespaceRegex))
            }

            val span = makeSpan(classes, elements, options)
            node.attributes.forEach { (attr, value) ->
                if (attr != "class") {
                    span.setAttribute(attr, value)
                }
            }
            span
        },
    )
}
