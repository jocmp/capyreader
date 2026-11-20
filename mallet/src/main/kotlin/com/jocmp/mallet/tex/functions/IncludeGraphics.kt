package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.CssStyle
import com.jocmp.mallet.tex.Img
import com.jocmp.mallet.tex.Measurement
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.validUnit

private val unitlessSizeRegex = Regex("^[-+]? *(\\d+(\\.\\d*)?|\\.\\d+)$")

private val sizeRegex = Regex("([-+]?) *(\\d+(?:\\.\\d*)?|\\.\\d+) *([a-z]{2})")

private val pathPrefixRegex = Regex("^.*[\\\\/]")

internal fun defineIncludeGraphics() {
    defineFunction(
        type = "includegraphics",
        names = listOf("\\includegraphics"),
        numArgs = 1,
        numOptionalArgs = 1,
        argTypes = listOf(ArgType.RAW, ArgType.URL),
        allowedInText = false,
        handler = { context, args, optArgs ->
            val parser = context.parser
            var width = Measurement(number = 0.0, unit = "em")
            var height = Measurement(number = 0.9, unit = "em")
            var totalheight = Measurement(number = 0.0, unit = "em")
            var alt = ""

            val optArg = optArgs.getOrNull(0)
            if (optArg != null) {
                val attributeStr = assertNodeType<ParseNode.Raw>(optArg).string

                val attributes = attributeStr.split(",")
                attributes.forEach { attribute ->
                    val keyVal = attribute.split("=")
                    if (keyVal.size == 2) {
                        val str = keyVal[1].trim()
                        when (keyVal[0].trim()) {
                            "alt" -> alt = str
                            "width" -> width = sizeData(str)
                            "height" -> height = sizeData(str)
                            "totalheight" -> totalheight = sizeData(str)
                            else -> throw ParseError("Invalid key: '" + keyVal[0] + "' in \\includegraphics.")
                        }
                    }
                }
            }

            val src = assertNodeType<ParseNode.Url>(args[0]).url

            if (alt == "") {
                alt = src
                alt = alt.replaceFirst(pathPrefixRegex, "")
                alt = alt.substring(0, maxOf(alt.lastIndexOf('.'), 0))
            }

            if (!parser.settings.isTrusted(command = "\\includegraphics", url = src)) {
                parser.formatUnsupportedCmd("\\includegraphics")
            } else {
                ParseNode.IncludeGraphics(
                    mode = parser.mode,
                    alt = alt,
                    width = width,
                    height = height,
                    totalheight = totalheight,
                    src = src,
                )
            }
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.IncludeGraphics>(group)
            val height = calculateSize(node.height, options)
            var depth = 0.0

            if (node.totalheight.number > 0) {
                depth = calculateSize(node.totalheight, options) - height
            }

            var width = 0.0
            if (node.width.number > 0) {
                width = calculateSize(node.width, options)
            }

            val style = CssStyle(height = makeEm(height + depth))
            if (width > 0) {
                style.width = makeEm(width)
            }
            if (depth > 0) {
                style.verticalAlign = makeEm(-depth)
            }

            val img = Img(node.src, node.alt, style)
            img.height = height
            img.depth = depth

            img
        },
    )
}

private fun sizeData(str: String): Measurement {
    if (unitlessSizeRegex.containsMatchIn(str)) {
        return Measurement(number = str.toDoubleOrNull() ?: Double.NaN, unit = "bp")
    } else {
        val match = sizeRegex.find(str) ?: throw ParseError("Invalid size: '$str' in \\includegraphics")
        val data =
            Measurement(
                number = (match.groupValues[1] + match.groupValues[2]).toDouble(),
                unit = match.groupValues[3],
            )
        if (!validUnit(data)) {
            throw ParseError("Invalid unit: '" + data.unit + "' in \\includegraphics.")
        }
        return data
    }
}
