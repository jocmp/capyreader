package com.jocmp.mallet.tex

/** KaTeX's stretchy.ts: stretchy wide elements rendered from SVG images. */
object Stretchy {
    fun stretchySvg(group: ParseNode, options: Options): Span {
        val (span, minWidth, height) = buildSvgSpan(group, options)

        span.height = height
        span.style.height = makeEm(height)
        if (minWidth > 0) {
            span.style.minWidth = makeEm(minWidth)
        }

        return span
    }

    fun stretchyEnclose(inner: HtmlDomNode, label: String, topPad: Double, bottomPad: Double, options: Options): Span {
        val img: Span
        val totalHeight = inner.height + inner.depth + topPad + bottomPad

        if (Regex("fbox|color|angl").containsMatchIn(label)) {
            img = makeSpan(listOf("katex-stretchy", label), emptyList(), options)

            if (label == "fbox") {
                val color = if (!options.color.isNullOrEmpty()) options.getColor() else null
                if (!color.isNullOrEmpty()) {
                    img.style.borderColor = color
                }
            }
        } else {
            val lines = mutableListOf<SvgChildNode>()
            if (Regex("^[bx]cancel$").containsMatchIn(label)) {
                lines.add(
                    LineNode(
                        mapOf(
                            "x1" to "0",
                            "y1" to "0",
                            "x2" to "100%",
                            "y2" to "100%",
                            "stroke-width" to "0.046em",
                        ),
                    ),
                )
            }

            if (Regex("^x?cancel$").containsMatchIn(label)) {
                lines.add(
                    LineNode(
                        mapOf(
                            "x1" to "0",
                            "y1" to "100%",
                            "x2" to "100%",
                            "y2" to "0",
                            "stroke-width" to "0.046em",
                        ),
                    ),
                )
            }

            val svgNode =
                SvgNode(
                    lines,
                    mutableMapOf(
                        "width" to "100%",
                        "height" to makeEm(totalHeight),
                    ),
                )

            img = makeSvgSpan(emptyList(), listOf(svgNode), options)
        }

        img.height = totalHeight
        img.style.height = makeEm(totalHeight)

        return img
    }

    private data class SvgSpanData(
        val span: Span,
        val minWidth: Double,
        val height: Double,
    )

    private class ImageData(
        val paths: List<String>,
        val minWidth: Double,
        val viewBoxHeight: Int,
        val align: String? = null,
    )

    private val katexImagesData: Map<String, ImageData> =
        mapOf(
            "overrightarrow" to ImageData(listOf("rightarrow"), 0.888, 522, "xMaxYMin"),
            "overleftarrow" to ImageData(listOf("leftarrow"), 0.888, 522, "xMinYMin"),
            "underrightarrow" to ImageData(listOf("rightarrow"), 0.888, 522, "xMaxYMin"),
            "underleftarrow" to ImageData(listOf("leftarrow"), 0.888, 522, "xMinYMin"),
            "xrightarrow" to ImageData(listOf("rightarrow"), 1.469, 522, "xMaxYMin"),
            "\\cdrightarrow" to ImageData(listOf("rightarrow"), 3.0, 522, "xMaxYMin"),
            "xleftarrow" to ImageData(listOf("leftarrow"), 1.469, 522, "xMinYMin"),
            "\\cdleftarrow" to ImageData(listOf("leftarrow"), 3.0, 522, "xMinYMin"),
            "Overrightarrow" to ImageData(listOf("doublerightarrow"), 0.888, 560, "xMaxYMin"),
            "xRightarrow" to ImageData(listOf("doublerightarrow"), 1.526, 560, "xMaxYMin"),
            "xLeftarrow" to ImageData(listOf("doubleleftarrow"), 1.526, 560, "xMinYMin"),
            "overleftharpoon" to ImageData(listOf("leftharpoon"), 0.888, 522, "xMinYMin"),
            "xleftharpoonup" to ImageData(listOf("leftharpoon"), 0.888, 522, "xMinYMin"),
            "xleftharpoondown" to ImageData(listOf("leftharpoondown"), 0.888, 522, "xMinYMin"),
            "overrightharpoon" to ImageData(listOf("rightharpoon"), 0.888, 522, "xMaxYMin"),
            "xrightharpoonup" to ImageData(listOf("rightharpoon"), 0.888, 522, "xMaxYMin"),
            "xrightharpoondown" to ImageData(listOf("rightharpoondown"), 0.888, 522, "xMaxYMin"),
            "xlongequal" to ImageData(listOf("longequal"), 0.888, 334, "xMinYMin"),
            "\\cdlongequal" to ImageData(listOf("longequal"), 3.0, 334, "xMinYMin"),
            "xtwoheadleftarrow" to ImageData(listOf("twoheadleftarrow"), 0.888, 334, "xMinYMin"),
            "xtwoheadrightarrow" to ImageData(listOf("twoheadrightarrow"), 0.888, 334, "xMaxYMin"),
            "overleftrightarrow" to ImageData(listOf("leftarrow", "rightarrow"), 0.888, 522),
            "overbrace" to ImageData(listOf("leftbrace", "midbrace", "rightbrace"), 1.6, 548),
            "underbrace" to ImageData(listOf("leftbraceunder", "midbraceunder", "rightbraceunder"), 1.6, 548),
            "underleftrightarrow" to ImageData(listOf("leftarrow", "rightarrow"), 0.888, 522),
            "xleftrightarrow" to ImageData(listOf("leftarrow", "rightarrow"), 1.75, 522),
            "xLeftrightarrow" to ImageData(listOf("doubleleftarrow", "doublerightarrow"), 1.75, 560),
            "xrightleftharpoons" to ImageData(listOf("leftharpoondownplus", "rightharpoonplus"), 1.75, 716),
            "xleftrightharpoons" to ImageData(listOf("leftharpoonplus", "rightharpoondownplus"), 1.75, 716),
            "xhookleftarrow" to ImageData(listOf("leftarrow", "righthook"), 1.08, 522),
            "xhookrightarrow" to ImageData(listOf("lefthook", "rightarrow"), 1.08, 522),
            "overlinesegment" to ImageData(listOf("leftlinesegment", "rightlinesegment"), 0.888, 522),
            "underlinesegment" to ImageData(listOf("leftlinesegment", "rightlinesegment"), 0.888, 522),
            "overbracket" to ImageData(listOf("leftbracketover", "rightbracketover"), 1.6, 440),
            "underbracket" to ImageData(listOf("leftbracketunder", "rightbracketunder"), 1.6, 410),
            "overgroup" to ImageData(listOf("leftgroup", "rightgroup"), 0.888, 342),
            "undergroup" to ImageData(listOf("leftgroupunder", "rightgroupunder"), 0.888, 342),
            "xmapsto" to ImageData(listOf("leftmapsto", "rightarrow"), 1.5, 522),
            "xtofrom" to ImageData(listOf("leftToFrom", "rightToFrom"), 1.75, 528),
            "xrightleftarrows" to ImageData(listOf("baraboveleftarrow", "rightarrowabovebar"), 1.75, 901),
            "xrightequilibrium" to ImageData(listOf("baraboveshortleftharpoon", "rightharpoonaboveshortbar"), 1.75, 716),
            "xleftequilibrium" to ImageData(listOf("shortbaraboveleftharpoon", "shortrightharpoonabovebar"), 1.75, 716),
        )

    private val wideAccentLabels = setOf("widehat", "widecheck", "widetilde", "utilde")

    private fun buildSvgSpan(group: ParseNode, options: Options): SvgSpanData {
        var viewBoxWidth = 400000
        val groupLabel: String
        val base: ParseNode?
        when (group) {
            is ParseNode.Accent -> {
                groupLabel = group.label
                base = group.base
            }
            is ParseNode.AccentUnder -> {
                groupLabel = group.label
                base = group.base
            }
            is ParseNode.XArrow -> {
                groupLabel = group.label
                base = null
            }
            is ParseNode.HorizBrace -> {
                groupLabel = group.label
                base = group.base
            }
            else -> throw IllegalStateException("Expected a stretchy group, but got ${group.type}")
        }
        val label = groupLabel.substring(1)
        if (label in wideAccentLabels && base != null) {
            val numChars = if (base is ParseNode.OrdGroup) base.body.size else 1
            val viewBoxHeight: Int
            val pathName: String
            val height: Double

            if (numChars > 5) {
                if (label == "widehat" || label == "widecheck") {
                    viewBoxHeight = 420
                    viewBoxWidth = 2364
                    height = 0.42
                    pathName = label + "4"
                } else {
                    viewBoxHeight = 312
                    viewBoxWidth = 2340
                    height = 0.34
                    pathName = "tilde4"
                }
            } else {
                val imgIndex = listOf(1, 1, 2, 2, 3, 3)[numChars]
                if (label == "widehat" || label == "widecheck") {
                    viewBoxWidth = listOf(0, 1062, 2364, 2364, 2364)[imgIndex]
                    viewBoxHeight = listOf(0, 239, 300, 360, 420)[imgIndex]
                    height = listOf(0.0, 0.24, 0.3, 0.3, 0.36, 0.42)[imgIndex]
                    pathName = label + imgIndex
                } else {
                    viewBoxWidth = listOf(0, 600, 1033, 2339, 2340)[imgIndex]
                    viewBoxHeight = listOf(0, 260, 286, 306, 312)[imgIndex]
                    height = listOf(0.0, 0.26, 0.286, 0.3, 0.306, 0.34)[imgIndex]
                    pathName = "tilde$imgIndex"
                }
            }
            val path = PathNode(pathName)
            val svgNode =
                SvgNode(
                    listOf(path),
                    mutableMapOf(
                        "width" to "100%",
                        "height" to makeEm(height),
                        "viewBox" to "0 0 $viewBoxWidth $viewBoxHeight",
                        "preserveAspectRatio" to "none",
                    ),
                )
            return SvgSpanData(
                span = makeSvgSpan(emptyList(), listOf(svgNode), options),
                minWidth = 0.0,
                height = height,
            )
        } else {
            val spans = mutableListOf<HtmlDomNode>()

            val data = katexImagesData[label] ?: throw IllegalStateException("No SVG data for \"$label\".")
            val paths = data.paths
            val minWidth = data.minWidth
            val viewBoxHeight = data.viewBoxHeight
            val height = viewBoxHeight / 1000.0

            val numSvgChildren = paths.size
            val widthClasses: List<String>
            val aligns: List<String?>
            if (numSvgChildren == 1) {
                if (data.align == null) {
                    throw IllegalStateException("Expected 4-tuple for single-path SVG data \"$label\".")
                }
                widthClasses = listOf("hide-tail")
                aligns = listOf(data.align)
            } else if (numSvgChildren == 2) {
                widthClasses = listOf("halfarrow-left", "halfarrow-right")
                aligns = listOf("xMinYMin", "xMaxYMin")
            } else if (numSvgChildren == 3) {
                widthClasses = listOf("brace-left", "brace-center", "brace-right")
                aligns = listOf("xMinYMin", "xMidYMin", "xMaxYMin")
            } else {
                throw IllegalStateException(
                    "Correct katexImagesData or update code here to support\n                    $numSvgChildren children.",
                )
            }

            for (i in 0 until numSvgChildren) {
                val path = PathNode(paths[i])

                val svgNode =
                    SvgNode(
                        listOf(path),
                        mutableMapOf(
                            "width" to "400em",
                            "height" to makeEm(height),
                            "viewBox" to "0 0 $viewBoxWidth $viewBoxHeight",
                            "preserveAspectRatio" to aligns[i] + " slice",
                        ),
                    )

                val span = makeSvgSpan(listOf(widthClasses[i]), listOf(svgNode), options)
                if (numSvgChildren == 1) {
                    return SvgSpanData(span, minWidth, height)
                } else {
                    span.style.height = makeEm(height)
                    spans.add(span)
                }
            }

            return SvgSpanData(
                span = makeSpan(listOf("katex-stretchy"), spans, options),
                minWidth = minWidth,
                height = height,
            )
        }
    }
}
