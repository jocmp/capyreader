package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Measurement
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.PathNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Stretchy
import com.jocmp.mallet.tex.SvgGeometry
import com.jocmp.mallet.tex.SvgNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeSvgSpan
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.wrapFragment
import kotlin.math.floor

internal fun defineEnclose() {
    defineFunction(
        type = "enclose",
        names = listOf("\\colorbox"),
        numArgs = 2,
        allowedInText = true,
        argTypes = listOf(ArgType.COLOR, ArgType.HBOX),
        handler = { context, args, _ ->
            val color = assertNodeType<ParseNode.ColorToken>(args[0]).color
            val body = args[1]
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = context.funcName,
                backgroundColor = color,
                body = body,
            )
        },
        htmlBuilder = ::encloseHtmlBuilder,
    )

    defineFunction(
        type = "enclose",
        names = listOf("\\fcolorbox"),
        numArgs = 3,
        allowedInText = true,
        argTypes = listOf(ArgType.COLOR, ArgType.COLOR, ArgType.HBOX),
        handler = { context, args, _ ->
            val borderColor = assertNodeType<ParseNode.ColorToken>(args[0]).color
            val backgroundColor = assertNodeType<ParseNode.ColorToken>(args[1]).color
            val body = args[2]
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = context.funcName,
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                body = body,
            )
        },
    )

    defineFunction(
        type = "enclose",
        names = listOf("\\fbox"),
        numArgs = 1,
        argTypes = listOf(ArgType.HBOX),
        allowedInText = true,
        handler = { context, args, _ ->
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = "\\fbox",
                body = args[0],
            )
        },
    )

    defineFunction(
        type = "enclose",
        names = listOf("\\cancel", "\\bcancel", "\\xcancel", "\\phase"),
        numArgs = 1,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = context.funcName,
                body = body,
            )
        },
    )

    defineFunction(
        type = "enclose",
        names = listOf("\\sout"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            if (context.parser.mode == Mode.MATH) {
                context.parser.settings.reportNonstrict("mathVsSout", "LaTeX's \\sout works only in text mode")
            }
            val body = args[0]
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = context.funcName,
                body = body,
            )
        },
    )

    defineFunction(
        type = "enclose",
        names = listOf("\\angl"),
        numArgs = 1,
        argTypes = listOf(ArgType.HBOX),
        allowedInText = false,
        handler = { context, args, _ ->
            ParseNode.Enclose(
                mode = context.parser.mode,
                label = "\\angl",
                body = args[0],
            )
        },
    )
}

private fun encloseHtmlBuilder(node: ParseNode, options: Options): HtmlDomNode {
    val group = assertNodeType<ParseNode.Enclose>(node)
    val inner = wrapFragment(buildGroup(group.body, options), options)

    val label = group.label.substring(1)
    var scale = options.sizeMultiplier
    val img: Span
    val imgShift: Double

    val isSingleChar = isCharacterBox(group.body)

    if (label == "sout") {
        img = makeSpan(listOf("katex-stretchy", "katex-sout"))
        img.height = options.fontMetrics().defaultRuleThickness / scale
        imgShift = -0.5 * options.fontMetrics().xHeight
    } else if (label == "phase") {
        val lineWeight = calculateSize(Measurement(number = 0.6, unit = "pt"), options)
        val clearance = calculateSize(Measurement(number = 0.35, unit = "ex"), options)

        val newOptions = options.havingBaseSizing()
        scale = scale / newOptions.sizeMultiplier

        val angleHeight = inner.height + inner.depth + lineWeight + clearance
        inner.style.paddingLeft = makeEm(angleHeight / 2 + lineWeight)

        val viewBoxHeight = floor(1000 * angleHeight * scale)
        val path = SvgGeometry.phasePath(viewBoxHeight)
        val svgNode =
            SvgNode(
                children = listOf(PathNode("phase", path)),
                attributes =
                    mutableMapOf(
                        "width" to "400em",
                        "height" to makeEm(viewBoxHeight / 1000),
                        "viewBox" to "0 0 400000 ${viewBoxHeight.toLong()}",
                        "preserveAspectRatio" to "xMinYMin slice",
                    ),
            )
        img = makeSvgSpan(listOf("hide-tail"), listOf(svgNode), options)
        img.style.height = makeEm(angleHeight)
        imgShift = inner.depth + lineWeight + clearance
    } else {
        if (label.contains("cancel")) {
            if (!isSingleChar) {
                inner.classes.add("cancel-pad")
            }
        } else if (label == "angl") {
            inner.classes.add("anglpad")
        } else {
            inner.classes.add("boxpad")
        }

        val topPad: Double
        val bottomPad: Double
        var ruleThickness = 0.0
        if (label.contains("box")) {
            ruleThickness =
                maxOf(
                    options.fontMetrics().fboxrule,
                    options.minRuleThickness,
                )
            topPad = options.fontMetrics().fboxsep + (if (label == "colorbox") 0.0 else ruleThickness)
            bottomPad = topPad
        } else if (label == "angl") {
            ruleThickness =
                maxOf(
                    options.fontMetrics().defaultRuleThickness,
                    options.minRuleThickness,
                )
            topPad = 4 * ruleThickness
            bottomPad = maxOf(0.0, 0.25 - inner.depth)
        } else {
            topPad = if (isSingleChar) 0.2 else 0.0
            bottomPad = topPad
        }

        img = Stretchy.stretchyEnclose(inner, label, topPad, bottomPad, options)
        if (label.contains("fbox") || label.contains("boxed") || label.contains("fcolorbox")) {
            img.style.borderStyle = "solid"
            img.style.borderWidth = makeEm(ruleThickness)
        } else if (label == "angl" && ruleThickness != 0.049) {
            img.style.borderTopWidth = makeEm(ruleThickness)
            img.style.borderRightWidth = makeEm(ruleThickness)
        }
        imgShift = inner.depth + bottomPad

        if (!group.backgroundColor.isNullOrEmpty()) {
            img.style.backgroundColor = group.backgroundColor
            if (!group.borderColor.isNullOrEmpty()) {
                img.style.borderColor = group.borderColor
            }
        }
    }

    val vlist =
        if (!group.backgroundColor.isNullOrEmpty()) {
            makeVList(
                VListParam.IndividualShift(
                    children =
                        listOf(
                            VListChild.Elem(elem = img, shift = imgShift),
                            VListChild.Elem(elem = inner, shift = 0.0),
                        ),
                ),
                options,
            )
        } else {
            val classes = if (label.contains("cancel") || label.contains("phase")) listOf("svg-align") else emptyList()
            makeVList(
                VListParam.IndividualShift(
                    children =
                        listOf(
                            VListChild.Elem(
                                elem = inner,
                                shift = 0.0,
                            ),
                            VListChild.Elem(
                                elem = img,
                                shift = imgShift,
                                wrapperClasses = classes,
                            ),
                        ),
                ),
                options,
            )
        }

    if (label.contains("cancel")) {
        vlist.height = inner.height
        vlist.depth = inner.depth
    }

    return if (label.contains("cancel") && !isSingleChar) {
        makeSpan(listOf("mord", "cancel-lap"), listOf(vlist), options)
    } else {
        makeSpan(listOf("mord"), listOf(vlist), options)
    }
}
