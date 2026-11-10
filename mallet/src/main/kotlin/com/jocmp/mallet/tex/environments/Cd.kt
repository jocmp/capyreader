package com.jocmp.mallet.tex.environments

import com.jocmp.mallet.tex.AlignSpec
import com.jocmp.mallet.tex.MacroString
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.assertSymbolNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.wrapFragment

private val cdArrowFunctionName =
    mapOf(
        ">" to "\\\\cdrightarrow",
        "<" to "\\\\cdleftarrow",
        "=" to "\\\\cdlongequal",
        "A" to "\\uparrow",
        "V" to "\\downarrow",
        "|" to "\\Vert",
        "." to "no arrow",
    )

fun defineCdEnvironment() {
    defineFunction(
        type = "cdlabel",
        names = listOf("\\\\cdleft", "\\\\cdright"),
        numArgs = 1,
        handler = { context, args, _ ->
            ParseNode.CdLabel(
                mode = context.parser.mode,
                side = context.funcName.substring(4),
                label = args[0],
            )
        },
        htmlBuilder = { group, options ->
            val group = assertNodeType<ParseNode.CdLabel>(group)
            val newOptions = options.havingStyle(options.style.sup())
            val label = wrapFragment(buildGroup(group.label, newOptions, options), options)
            label.classes.add("cd-label-" + group.side)
            label.style.bottom = makeEm(0.8 - label.depth)
            label.height = 0.0
            label.depth = 0.0
            label
        },
    )

    defineFunction(
        type = "cdlabelparent",
        names = listOf("\\\\cdparent"),
        numArgs = 1,
        handler = { context, args, _ ->
            ParseNode.CdLabelParent(
                mode = context.parser.mode,
                fragment = args[0],
            )
        },
        htmlBuilder = { group, options ->
            val group = assertNodeType<ParseNode.CdLabelParent>(group)
            val parent = wrapFragment(buildGroup(group.fragment, options), options)
            parent.classes.add("cd-vert-arrow")
            parent
        },
    )
}

internal fun parseCD(parser: Parser): ParseNode.Array {
    val parsedRows = mutableListOf<List<ParseNode>>()
    parser.gullet.beginGroup()
    parser.gullet.macros.set("\\cr", MacroString("\\\\\\relax"))
    parser.gullet.beginGroup()
    while (true) {
        parsedRows.add(parser.parseExpression(false, "\\\\"))
        parser.gullet.endGroup()
        parser.gullet.beginGroup()
        val next = parser.fetch().text
        if (next == "&" || next == "\\\\") {
            parser.consume()
        } else if (next == "\\end") {
            if (parsedRows[parsedRows.size - 1].isEmpty()) {
                parsedRows.removeAt(parsedRows.size - 1)
            }
            break
        } else {
            throw ParseError("Expected \\\\ or \\cr or \\end", parser.fetch())
        }
    }

    var row = mutableListOf<ParseNode>()
    val body = mutableListOf<MutableList<ParseNode>>(row)

    parsedRows.forEachIndexed { i, rowNodes ->
        var cell = newCell()

        var j = 0
        while (j < rowNodes.size) {
            if (!isStartOfArrow(rowNodes[j])) {
                cell.body.add(rowNodes[j])
            } else {
                row.add(cell.node)

                j += 1
                val arrowChar = assertSymbolNodeType(rowNodes.getOrNull(j)).text

                val labels =
                    listOf(
                        mutableListOf<ParseNode>(),
                        mutableListOf<ParseNode>(),
                    )

                if ("=|.".contains(arrowChar)) {
                    // Three "arrows", `@=`, `@|`, and `@.`, do not take labels.
                } else if ("<>AV".contains(arrowChar)) {
                    (0 until 2).forEach { labelNum ->
                        var inLabel = true
                        var k = j + 1
                        while (k < rowNodes.size) {
                            if (isLabelEnd(rowNodes[k], arrowChar)) {
                                inLabel = false
                                j = k
                                break
                            }
                            if (isStartOfArrow(rowNodes[k])) {
                                throw ParseError(
                                    "Missing a $arrowChar character to complete a CD arrow.",
                                    rowNodes[k],
                                )
                            }

                            labels[labelNum].add(rowNodes[k])
                            k += 1
                        }
                        if (inLabel) {
                            throw ParseError(
                                "Missing a $arrowChar character to complete a CD arrow.",
                                rowNodes[j],
                            )
                        }
                    }
                } else {
                    throw ParseError("Expected one of \"<>AV=|.\" after @", rowNodes[j])
                }

                val arrow =
                    cdArrow(
                        arrowChar,
                        labels.map { ParseNode.OrdGroup(mode = Mode.MATH, body = it) },
                        parser,
                    )

                val wrappedArrow =
                    ParseNode.Styling(
                        mode = Mode.MATH,
                        body = listOf(arrow),
                        style = "display",
                        resetFont = true,
                    )
                row.add(wrappedArrow)
                cell = newCell()
            }
            j += 1
        }
        if (i % 2 == 0) {
            row.add(cell.node)
        } else {
            row.removeFirstOrNull()
        }
        row = mutableListOf()
        body.add(row)
    }

    parser.gullet.endGroup()
    parser.gullet.endGroup()

    val cols =
        List(body[0].size) {
            AlignSpec.Align(
                align = "c",
                pregap = 0.25,
                postgap = 0.25,
            )
        }

    return ParseNode.Array(
        mode = Mode.MATH,
        body = body,
        arraystretch = 1.0,
        addJot = true,
        rowGaps = listOf(null),
        cols = cols,
        colSeparationType = "CD",
        hLinesBeforeRow = List(body.size + 1) { emptyList() },
    )
}

private class Cell(
    val body: MutableList<ParseNode>,
    val node: ParseNode.Styling,
)

private fun newCell(): Cell {
    val body = mutableListOf<ParseNode>()
    return Cell(
        body = body,
        node =
            ParseNode.Styling(
                body = body,
                mode = Mode.MATH,
                style = "display",
                resetFont = true,
            ),
    )
}

private fun isStartOfArrow(node: ParseNode): Boolean = node is ParseNode.TextOrd && node.text == "@"

private fun isLabelEnd(
    node: ParseNode,
    endChar: String,
): Boolean = (node is ParseNode.MathOrd || node is ParseNode.Atom) && (node as ParseNode.Symbol).text == endChar

private fun cdArrow(
    arrowChar: String,
    labels: List<ParseNode.OrdGroup>,
    parser: Parser,
): ParseNode {
    val funcName = cdArrowFunctionName[arrowChar]
    return when (funcName) {
        "\\\\cdrightarrow", "\\\\cdleftarrow" ->
            parser.callFunction(funcName, listOf(labels[0]), listOf(labels[1]))
        "\\uparrow", "\\downarrow" -> {
            val leftLabel = parser.callFunction("\\\\cdleft", listOf(labels[0]), emptyList())
            val bareArrow =
                ParseNode.Atom(
                    mode = Mode.MATH,
                    family = "rel",
                    text = funcName,
                )
            val sizedArrow = parser.callFunction("\\Big", listOf(bareArrow), emptyList())
            val rightLabel = parser.callFunction("\\\\cdright", listOf(labels[1]), emptyList())
            val arrowGroup =
                ParseNode.OrdGroup(
                    mode = Mode.MATH,
                    body = listOf(leftLabel, sizedArrow, rightLabel),
                )
            parser.callFunction("\\\\cdparent", listOf(arrowGroup), emptyList())
        }
        "\\\\cdlongequal" -> parser.callFunction("\\\\cdlongequal", emptyList(), emptyList())
        "\\Vert" -> {
            val arrow = ParseNode.TextOrd(mode = Mode.MATH, text = "\\Vert")
            parser.callFunction("\\Big", listOf(arrow), emptyList())
        }
        else -> ParseNode.TextOrd(mode = Mode.MATH, text = " ")
    }
}
