package com.jocmp.mallet.tex.environments

import com.jocmp.mallet.tex.AlignSpec
import com.jocmp.mallet.tex.EnvContext
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.MacroDefinition
import com.jocmp.mallet.tex.MacroString
import com.jocmp.mallet.tex.Measurement
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.Token
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertCharacterGroup
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.assertSymbolNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.checkSymbolNodeType
import com.jocmp.mallet.tex.defineEnvironment
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.makeLineSpan
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

private val gatherEnvironments = setOf("gather", "gather*")

private val parseFloatRegex = Regex("^\\s*[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?")

fun defineArrayEnvironments() {
    defineEnvironment(
        type = "array",
        names = listOf("array", "darray"),
        numArgs = 1,
        handler = { context, args, _ ->
            val symNode = checkSymbolNodeType(args[0])
            val colalign: List<ParseNode> =
                if (symNode != null) listOf(args[0]) else assertNodeType<ParseNode.OrdGroup>(args[0]).body
            val cols: List<AlignSpec> =
                colalign.map { nde ->
                    val node = assertSymbolNodeType(nde)
                    val ca = node.text
                    if ("lcr".contains(ca)) {
                        AlignSpec.Align(align = ca)
                    } else if (ca == "|") {
                        AlignSpec.Separator(separator = "|")
                    } else if (ca == ":") {
                        AlignSpec.Separator(separator = ":")
                    } else {
                        throw ParseError("Unknown column alignment: $ca", nde)
                    }
                }
            val res =
                ArrayOptions(
                    cols = cols,
                    hskipBeforeAndAfter = true,
                    maxNumCols = cols.size,
                )
            parseArray(context.parser, res, dCellStyle(context.envName))
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names =
            listOf(
                "matrix",
                "pmatrix",
                "bmatrix",
                "Bmatrix",
                "vmatrix",
                "Vmatrix",
                "matrix*",
                "pmatrix*",
                "bmatrix*",
                "Bmatrix*",
                "vmatrix*",
                "Vmatrix*",
            ),
        numArgs = 0,
        handler = { context, _, _ ->
            val delimiters =
                mapOf(
                    "matrix" to null,
                    "pmatrix" to listOf("(", ")"),
                    "bmatrix" to listOf("[", "]"),
                    "Bmatrix" to listOf("\\{", "\\}"),
                    "vmatrix" to listOf("|", "|"),
                    "Vmatrix" to listOf("\\Vert", "\\Vert"),
                )[context.envName.replaceFirst("*", "")]
            var colAlign = "c"
            val payload =
                ArrayOptions(
                    hskipBeforeAndAfter = false,
                    cols = listOf(AlignSpec.Align(align = colAlign)),
                )
            if (context.envName[context.envName.length - 1] == '*') {
                val parser = context.parser
                parser.consumeSpaces()
                if (parser.fetch().text == "[") {
                    parser.consume()
                    parser.consumeSpaces()
                    colAlign = parser.fetch().text
                    if (!"lcr".contains(colAlign)) {
                        throw ParseError("Expected l or c or r", parser.fetch())
                    }
                    parser.consume()
                    parser.consumeSpaces()
                    parser.expect("]")
                    parser.consume()
                    payload.cols = listOf(AlignSpec.Align(align = colAlign))
                }
            }
            val res = parseArray(context.parser, payload, dCellStyle(context.envName))
            val numCols = maxOf(0, res.body.maxOfOrNull { row -> row.size } ?: 0)
            res.cols = List(numCols) { AlignSpec.Align(align = colAlign) }
            if (delimiters != null) {
                ParseNode.LeftRight(
                    mode = context.mode,
                    body = listOf(res),
                    left = delimiters[0],
                    right = delimiters[1],
                    rightColor = null,
                )
            } else {
                res
            }
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("smallmatrix"),
        numArgs = 0,
        handler = { context, _, _ ->
            val payload = ArrayOptions(arraystretch = 0.5)
            val res = parseArray(context.parser, payload, "script")
            res.colSeparationType = "small"
            res
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("subarray"),
        numArgs = 1,
        handler = { context, args, _ ->
            val symNode = checkSymbolNodeType(args[0])
            val colalign: List<ParseNode> =
                if (symNode != null) listOf(args[0]) else assertNodeType<ParseNode.OrdGroup>(args[0]).body
            val cols: List<AlignSpec> =
                colalign.map { nde ->
                    val node = assertSymbolNodeType(nde)
                    val ca = node.text
                    if ("lc".contains(ca)) {
                        AlignSpec.Align(align = ca)
                    } else {
                        throw ParseError("Unknown column alignment: $ca", nde)
                    }
                }
            if (cols.size > 1) {
                throw ParseError("{subarray} can contain only one column")
            }
            val payload =
                ArrayOptions(
                    cols = cols,
                    hskipBeforeAndAfter = false,
                    arraystretch = 0.5,
                )
            val res = parseArray(context.parser, payload, "script")
            if (res.body.isNotEmpty() && res.body[0].size > 1) {
                throw ParseError("{subarray} can contain only one column")
            }
            res
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names =
            listOf(
                "cases",
                "dcases",
                "rcases",
                "drcases",
            ),
        numArgs = 0,
        handler = { context, _, _ ->
            val payload =
                ArrayOptions(
                    arraystretch = 1.2,
                    cols =
                        listOf(
                            AlignSpec.Align(
                                align = "l",
                                pregap = 0.0,
                                postgap = 1.0,
                            ),
                            AlignSpec.Align(
                                align = "l",
                                pregap = 0.0,
                                postgap = 0.0,
                            ),
                        ),
                )
            val res = parseArray(context.parser, payload, dCellStyle(context.envName))
            ParseNode.LeftRight(
                mode = context.mode,
                body = listOf(res),
                left = if (context.envName.contains("r")) "." else "\\{",
                right = if (context.envName.contains("r")) "\\}" else ".",
                rightColor = null,
            )
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("align", "align*", "aligned", "split"),
        numArgs = 0,
        handler = ::alignedHandler,
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("gathered", "gather", "gather*"),
        numArgs = 0,
        handler = { context, _, _ ->
            if (context.envName in gatherEnvironments) {
                validateAmsEnvironmentContext(context)
            }
            val res =
                ArrayOptions(
                    cols = listOf(AlignSpec.Align(align = "c")),
                    addJot = true,
                    colSeparationType = "gather",
                    autoTag = getAutoTag(context.envName),
                    emptySingleRow = true,
                    leqno = context.parser.settings.leqno,
                )
            parseArray(context.parser, res, "display")
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("alignat", "alignat*", "alignedat"),
        numArgs = 1,
        handler = ::alignedHandler,
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("equation", "equation*"),
        numArgs = 0,
        handler = { context, _, _ ->
            validateAmsEnvironmentContext(context)
            val res =
                ArrayOptions(
                    autoTag = getAutoTag(context.envName),
                    emptySingleRow = true,
                    singleRow = true,
                    maxNumCols = 1,
                    leqno = context.parser.settings.leqno,
                )
            parseArray(context.parser, res, "display")
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineEnvironment(
        type = "array",
        names = listOf("CD"),
        numArgs = 0,
        handler = { context, _, _ ->
            validateAmsEnvironmentContext(context)
            parseCD(context.parser)
        },
        htmlBuilder = ::arrayHtmlBuilder,
    )

    defineFunction(
        type = "text",
        names = listOf("\\hline", "\\hdashline"),
        numArgs = 0,
        allowedInText = true,
        allowedInMath = true,
        handler = { context, _, _ ->
            throw ParseError("${context.funcName} valid only within array environment")
        },
    )
}

private class ArrayOptions(
    val hskipBeforeAndAfter: Boolean? = null,
    val addJot: Boolean? = null,
    var cols: List<AlignSpec>? = null,
    val arraystretch: Double? = null,
    val colSeparationType: String? = null,
    val autoTag: Boolean? = null,
    val singleRow: Boolean = false,
    val emptySingleRow: Boolean = false,
    val maxNumCols: Int? = null,
    val leqno: Boolean? = null,
)

private class OutRow(
    val cells: MutableList<HtmlDomNode?>,
    var height: Double,
    var depth: Double,
    var pos: Double,
)

private class HLine(val pos: Double, val isDashed: Boolean)

private fun getHLines(parser: Parser): List<Boolean> {
    val hlineInfo = mutableListOf<Boolean>()
    parser.consumeSpaces()
    var nxt = parser.fetch().text
    if (nxt == "\\relax") {
        parser.consume()
        parser.consumeSpaces()
        nxt = parser.fetch().text
    }
    while (nxt == "\\hline" || nxt == "\\hdashline") {
        parser.consume()
        hlineInfo.add(nxt == "\\hdashline")
        parser.consumeSpaces()
        nxt = parser.fetch().text
    }
    return hlineInfo
}

private fun validateAmsEnvironmentContext(context: EnvContext) {
    val settings = context.parser.settings
    if (!settings.displayMode) {
        throw ParseError("{${context.envName}} can be used only in display mode.")
    }
}

private fun getAutoTag(name: String): Boolean? {
    if (!name.contains("ed")) {
        return !name.contains("*")
    }
    return null
}

private fun parseArray(
    parser: Parser,
    options: ArrayOptions,
    style: String?,
): ParseNode.Array {
    val hskipBeforeAndAfter = options.hskipBeforeAndAfter
    val addJot = options.addJot
    val cols = options.cols
    var arraystretch = options.arraystretch
    val colSeparationType = options.colSeparationType
    val autoTag = options.autoTag
    val singleRow = options.singleRow
    val emptySingleRow = options.emptySingleRow
    val maxNumCols = options.maxNumCols
    val leqno = options.leqno

    parser.gullet.beginGroup()
    if (!singleRow) {
        parser.gullet.macros.set("\\cr", MacroString("\\\\\\relax"))
    }

    if (arraystretch == null || arraystretch == 0.0) {
        val stretch = parser.gullet.expandMacroAsText("\\arraystretch")
        if (stretch == null) {
            arraystretch = 1.0
        } else {
            arraystretch = parseFloat(stretch)
            if (arraystretch.isNaN() || arraystretch == 0.0 || arraystretch < 0) {
                throw ParseError("Invalid \\arraystretch: $stretch")
            }
        }
    }

    parser.gullet.beginGroup()

    var row = mutableListOf<ParseNode>()
    val body = mutableListOf<MutableList<ParseNode>>(row)
    val rowGaps = mutableListOf<Measurement?>()
    val hLinesBeforeRow = mutableListOf<List<Boolean>>()
    val tags: MutableList<Any>? = if (autoTag != null) mutableListOf() else null

    fun beginRow() {
        if (autoTag == true) {
            parser.gullet.macros.set("\\@eqnsw", MacroString("1"), true)
        }
    }

    fun endRow() {
        if (tags != null) {
            if (isTruthy(parser.gullet.macros.get("\\df@tag"))) {
                tags.add(parser.subparse(listOf(Token("\\df@tag"))))
                parser.gullet.macros.set("\\df@tag", null, true)
            } else {
                tags.add(autoTag == true && parser.gullet.macros.get("\\@eqnsw") == MacroString("1"))
            }
        }
    }
    beginRow()

    hLinesBeforeRow.add(getHLines(parser))

    while (true) {
        val cellBody = parser.parseExpression(false, if (singleRow) "\\end" else "\\\\")
        parser.gullet.endGroup()
        parser.gullet.beginGroup()
        var cell: ParseNode = ParseNode.OrdGroup(mode = parser.mode, body = cellBody.toMutableList())
        if (style != null) {
            cell =
                ParseNode.Styling(
                    mode = parser.mode,
                    style = style,
                    resetFont = true,
                    body = listOf(cell),
                )
        }
        row.add(cell)
        val next = parser.fetch().text
        if (next == "&") {
            if (maxNumCols != null && maxNumCols != 0 && row.size == maxNumCols) {
                if (singleRow || !colSeparationType.isNullOrEmpty()) {
                    throw ParseError("Too many tab characters: &", parser.fetch())
                } else {
                    parser.settings.reportNonstrict(
                        "textEnv",
                        "Too few columns specified in the {array} column argument.",
                    )
                }
            }
            parser.consume()
        } else if (next == "\\end") {
            endRow()
            val lastTag = tags?.lastOrNull()
            if (row.size == 1 &&
                cell is ParseNode.Styling &&
                cell.body.size == 1 &&
                cell.body[0] is ParseNode.OrdGroup &&
                (cell.body[0] as ParseNode.OrdGroup).body.isEmpty() &&
                (body.size > 1 || !emptySingleRow) &&
                lastTag !is List<*> &&
                !(autoTag == true && lastTag == true)
            ) {
                body.removeAt(body.size - 1)
            }
            if (hLinesBeforeRow.size < body.size + 1) {
                hLinesBeforeRow.add(emptyList())
            }
            break
        } else if (next == "\\\\") {
            parser.consume()
            var size: ParseNode.Size? = null
            if (parser.gullet.future().text != " ") {
                size = parser.parseSizeGroup(true)
            }
            rowGaps.add(size?.value)
            endRow()

            hLinesBeforeRow.add(getHLines(parser))

            row = mutableListOf()
            body.add(row)
            beginRow()
        } else {
            throw ParseError("Expected & or \\\\ or \\cr or \\end", parser.fetch())
        }
    }

    parser.gullet.endGroup()
    parser.gullet.endGroup()

    return ParseNode.Array(
        mode = parser.mode,
        addJot = addJot,
        arraystretch = arraystretch,
        body = body,
        cols = cols,
        rowGaps = rowGaps,
        hskipBeforeAndAfter = hskipBeforeAndAfter,
        hLinesBeforeRow = hLinesBeforeRow,
        colSeparationType = colSeparationType,
        tags = tags,
        leqno = leqno,
    )
}

private fun isTruthy(definition: MacroDefinition?): Boolean =
    definition != null && !(definition is MacroString && definition.text.isEmpty())

private fun parseFloat(value: String): Double =
    parseFloatRegex.find(value)?.value?.trim()?.toDoubleOrNull() ?: Double.NaN

private fun dCellStyle(envName: String): String =
    if (envName.take(1) == "d") {
        "display"
    } else {
        "text"
    }

private fun arrayHtmlBuilder(
    group: ParseNode,
    options: Options,
): HtmlDomNode {
    val group = assertNodeType<ParseNode.Array>(group)
    val nr = group.body.size
    val hLinesBeforeRow = group.hLinesBeforeRow
    var nc = 0
    val body = arrayOfNulls<OutRow>(nr)
    val hlines = mutableListOf<HLine>()
    val ruleThickness =
        maxOf(
            options.fontMetrics().arrayRuleWidth,
            options.minRuleThickness,
        )

    val pt = 1 / options.fontMetrics().ptPerEm
    var arraycolsep = 5 * pt
    if (group.colSeparationType == "small") {
        val localMultiplier = options.havingStyle(Style.SCRIPT).sizeMultiplier
        arraycolsep = 0.2778 * (localMultiplier / options.sizeMultiplier)
    }

    val baselineskip =
        if (group.colSeparationType == "CD") {
            calculateSize(Measurement(number = 3.0, unit = "ex"), options)
        } else {
            12 * pt
        }
    val jot = 3 * pt
    val arrayskip = group.arraystretch * baselineskip
    val arstrutHeight = 0.7 * arrayskip
    val arstrutDepth = 0.3 * arrayskip

    var totalHeight = 0.0

    fun setHLinePos(hlinesInGap: List<Boolean>) {
        hlinesInGap.forEachIndexed { i, isDashed ->
            if (i > 0) {
                totalHeight += 0.25
            }
            hlines.add(HLine(pos = totalHeight, isDashed = isDashed))
        }
    }
    setHLinePos(hLinesBeforeRow[0])

    group.body.forEachIndexed { r, inrow ->
        var height = arstrutHeight
        var depth = arstrutDepth

        if (nc < inrow.size) {
            nc = inrow.size
        }

        val outrow =
            OutRow(
                cells = MutableList(inrow.size) { null },
                height = 0.0,
                depth = 0.0,
                pos = 0.0,
            )
        inrow.forEachIndexed { c, cell ->
            val elt = buildGroup(cell, options)
            if (depth < elt.depth) {
                depth = elt.depth
            }
            if (height < elt.height) {
                height = elt.height
            }
            outrow.cells[c] = elt
        }

        val rowGap = group.rowGaps.getOrNull(r)
        var gap = 0.0
        if (rowGap != null) {
            gap = calculateSize(rowGap, options)
            if (gap > 0) {
                gap += arstrutDepth
                if (depth < gap) {
                    depth = gap
                }
                gap = 0.0
            }
        }
        if (group.addJot == true && r < group.body.size - 1) {
            depth += jot
        }

        outrow.height = height
        outrow.depth = depth
        totalHeight += height
        outrow.pos = totalHeight
        totalHeight += depth + gap
        body[r] = outrow

        setHLinePos(hLinesBeforeRow[r + 1])
    }

    val offset = totalHeight / 2 + options.fontMetrics().axisHeight
    val colDescriptions = group.cols.orEmpty()
    val cols = mutableListOf<HtmlDomNode>()
    var colSep: Span
    val tagSpans = mutableListOf<VListChild.Elem>()

    val tags = group.tags
    if (tags != null && tags.any { tag -> isTagTruthy(tag) }) {
        (0 until nr).forEach { r ->
            val rw = body[r]!!
            val shift = rw.pos - offset
            val tag = tags[r]
            val tagSpan =
                if (tag == true) {
                    makeSpan(listOf("eqn-num"), emptyList(), options)
                } else if (tag == false) {
                    makeSpan(emptyList(), emptyList(), options)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    makeSpan(emptyList(), buildExpression(tag as List<ParseNode>, options, true), options)
                }
            tagSpan.depth = rw.depth
            tagSpan.height = rw.height
            tagSpans.add(VListChild.Elem(elem = tagSpan, shift = shift))
        }
    }

    var c = 0
    var colDescrNum = 0
    while (c < nc || colDescrNum < colDescriptions.size) {
        var colDescr: AlignSpec? = colDescriptions.getOrNull(colDescrNum)

        var firstSeparator = true
        while (colDescr is AlignSpec.Separator) {
            if (!firstSeparator) {
                colSep = makeSpan(listOf("arraycolsep"), emptyList())
                colSep.style.width = makeEm(options.fontMetrics().doubleRuleSep)
                cols.add(colSep)
            }

            if (colDescr.separator == "|" || colDescr.separator == ":") {
                val lineType = if (colDescr.separator == "|") "solid" else "dashed"
                val separator = makeSpan(listOf("vertical-separator"), emptyList(), options)
                separator.style.height = makeEm(totalHeight)
                separator.style.borderRightWidth = makeEm(ruleThickness)
                separator.style.borderRightStyle = lineType
                separator.style.margin = "0 ${makeEm(-ruleThickness / 2)}"
                val shift = totalHeight - offset
                if (shift != 0.0) {
                    separator.style.verticalAlign = makeEm(-shift)
                }

                cols.add(separator)
            } else {
                throw ParseError("Invalid separator type: " + colDescr.separator)
            }

            colDescrNum += 1
            colDescr = colDescriptions.getOrNull(colDescrNum)
            firstSeparator = false
        }

        if (c >= nc) {
            c += 1
            colDescrNum += 1
            continue
        }

        val alignDescr = colDescr as? AlignSpec.Align
        var sepwidth: Double
        if (c > 0 || group.hskipBeforeAndAfter == true) {
            sepwidth = alignDescr?.pregap ?: arraycolsep
            if (sepwidth != 0.0) {
                colSep = makeSpan(listOf("arraycolsep"), emptyList())
                colSep.style.width = makeEm(sepwidth)
                cols.add(colSep)
            }
        }

        val colElems = mutableListOf<VListChild.Elem>()
        (0 until nr).forEach { r ->
            val row = body[r]!!
            val elem = row.cells.getOrNull(c) ?: return@forEach
            val shift = row.pos - offset
            elem.depth = row.depth
            elem.height = row.height
            colElems.add(VListChild.Elem(elem = elem, shift = shift))
        }

        val colVList = makeVList(VListParam.IndividualShift(children = colElems), options)
        val colSpan =
            makeSpan(
                listOf("col-align-" + (alignDescr?.align?.takeIf { it.isNotEmpty() } ?: "c")),
                listOf(colVList),
            )
        cols.add(colSpan)

        if (c < nc - 1 || group.hskipBeforeAndAfter == true) {
            sepwidth = alignDescr?.postgap ?: arraycolsep
            if (sepwidth != 0.0) {
                colSep = makeSpan(listOf("arraycolsep"), emptyList())
                colSep.style.width = makeEm(sepwidth)
                cols.add(colSep)
            }
        }

        c += 1
        colDescrNum += 1
    }

    var tableBody: HtmlDomNode = makeSpan(listOf("mtable"), cols)

    if (hlines.isNotEmpty()) {
        val line = makeLineSpan("katex-hline", options, ruleThickness)
        val dashes = makeLineSpan("katex-hdashline", options, ruleThickness)
        val vListElems = mutableListOf(VListChild.Elem(elem = tableBody, shift = 0.0))
        while (hlines.isNotEmpty()) {
            val hline = hlines.removeAt(hlines.size - 1)
            val lineShift = hline.pos - offset
            if (hline.isDashed) {
                vListElems.add(VListChild.Elem(elem = dashes, shift = lineShift))
            } else {
                vListElems.add(VListChild.Elem(elem = line, shift = lineShift))
            }
        }

        tableBody = makeVList(VListParam.IndividualShift(children = vListElems), options)
    }

    if (tagSpans.isEmpty()) {
        return makeSpan(listOf("mord"), listOf(tableBody), options)
    } else {
        val eqnNumCol = makeVList(VListParam.IndividualShift(children = tagSpans), options)
        val tagCol = makeSpan(listOf("katex-tag"), listOf(eqnNumCol), options)
        return makeFragment(listOf(tableBody, tagCol))
    }
}

private fun isTagTruthy(tag: Any): Boolean = tag is List<*> || tag == true

private fun alignedHandler(
    context: EnvContext,
    args: List<ParseNode>,
    @Suppress("UNUSED_PARAMETER") optArgs: List<ParseNode?>,
): ParseNode {
    if (!context.envName.contains("ed")) {
        validateAmsEnvironmentContext(context)
    }
    val cols = mutableListOf<AlignSpec>()
    val isSplit = context.envName == "split"
    val res =
        parseArray(
            context.parser,
            ArrayOptions(
                cols = cols,
                addJot = true,
                autoTag = if (isSplit) null else getAutoTag(context.envName),
                emptySingleRow = true,
                colSeparationType = if (context.envName.contains("at")) "alignat" else "align",
                maxNumCols = if (isSplit) 2 else null,
                leqno = context.parser.settings.leqno,
            ),
            "display",
        )

    var numMaths = 0
    var numCols = 0
    val emptyGroup =
        ParseNode.OrdGroup(
            mode = context.mode,
            body = emptyList(),
        )
    val firstArg = args.getOrNull(0)
    if (firstArg != null && firstArg is ParseNode.OrdGroup) {
        val message = "Number of columns should be a positive integer"
        val numColumns = assertCharacterGroup(firstArg, message)
        if (!Regex("^[0-9]+$").matches(numColumns) || numColumns.toDouble() < 1) {
            throw ParseError(message, firstArg)
        }
        numMaths = numColumns.toDouble().toInt()
        numCols = numMaths * 2
    }
    val isAligned = numCols == 0
    res.body.forEach { row ->
        var i = 1
        while (i < row.size) {
            val styling = assertNodeType<ParseNode.Styling>(row[i])
            val ordgroup = assertNodeType<ParseNode.OrdGroup>(styling.body[0])
            // parseArray builds each cell's OrdGroup body as a MutableList so it can be prepended here.
            (ordgroup.body as MutableList<ParseNode>).add(0, emptyGroup)
            i += 2
        }
        if (!isAligned) {
            val curMaths = row.size / 2.0
            if (numMaths < curMaths) {
                throw ParseError(
                    "Too many math in a row: expected $numMaths, but got ${formatNumber(curMaths)}",
                    row[0],
                )
            }
        } else if (numCols < row.size) {
            numCols = row.size
        }
    }

    (0 until numCols).forEach { i ->
        var align = "r"
        var pregap = 0.0
        if (i % 2 == 1) {
            align = "l"
        } else if (i > 0 && isAligned) {
            pregap = 1.0
        }
        cols.add(
            AlignSpec.Align(
                align = align,
                pregap = pregap,
                postgap = 0.0,
            ),
        )
    }
    res.colSeparationType = if (isAligned) "align" else "alignat"
    return res
}

private fun formatNumber(n: Double): String = if (n == Math.floor(n)) n.toLong().toString() else n.toString()
