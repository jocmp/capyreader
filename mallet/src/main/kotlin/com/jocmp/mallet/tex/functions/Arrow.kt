package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Stretchy
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.wrapFragment

internal fun defineArrow() {
    defineFunction(
        type = "xArrow",
        names =
            listOf(
                "\\xleftarrow", "\\xrightarrow", "\\xLeftarrow", "\\xRightarrow",
                "\\xleftrightarrow", "\\xLeftrightarrow", "\\xhookleftarrow",
                "\\xhookrightarrow", "\\xmapsto", "\\xrightharpoondown",
                "\\xrightharpoonup", "\\xleftharpoondown", "\\xleftharpoonup",
                "\\xrightleftharpoons", "\\xleftrightharpoons", "\\xlongequal",
                "\\xtwoheadrightarrow", "\\xtwoheadleftarrow", "\\xtofrom",
                "\\xrightleftarrows", "\\xrightequilibrium", "\\xleftequilibrium",
                "\\\\cdrightarrow", "\\\\cdleftarrow", "\\\\cdlongequal",
            ),
        numArgs = 1,
        numOptionalArgs = 1,
        handler = { context, args, optArgs ->
            ParseNode.XArrow(
                mode = context.parser.mode,
                label = context.funcName,
                body = args[0],
                below = optArgs[0],
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.XArrow>(node)
            val style = options.style

            var newOptions = options.havingStyle(style.sup())
            val upperGroup = wrapFragment(buildGroup(group.body, newOptions, options), options)
            val arrowPrefix = if (group.label.take(2) == "\\x") "x" else "cd"
            upperGroup.classes.add("$arrowPrefix-arrow-pad")

            var lowerGroup: HtmlDomNode? = null
            if (group.below != null) {
                newOptions = options.havingStyle(style.sub())
                lowerGroup = wrapFragment(buildGroup(group.below, newOptions, options), options)
                lowerGroup.classes.add("$arrowPrefix-arrow-pad")
            }

            val arrowBody = Stretchy.stretchySvg(group, options)

            val arrowShift = -options.fontMetrics().axisHeight + 0.5 * arrowBody.height
            var upperShift = -options.fontMetrics().axisHeight - 0.5 * arrowBody.height - 0.111
            if (upperGroup.depth > 0.25 || group.label == "\\xleftequilibrium") {
                upperShift -= upperGroup.depth
            }

            val vlist =
                if (lowerGroup != null) {
                    val lowerShift =
                        -options.fontMetrics().axisHeight + lowerGroup.height + 0.5 * arrowBody.height + 0.111
                    makeVList(
                        VListParam.IndividualShift(
                            children =
                                listOf(
                                    VListChild.Elem(elem = upperGroup, shift = upperShift),
                                    VListChild.Elem(
                                        elem = arrowBody,
                                        shift = arrowShift,
                                        wrapperClasses = listOf("svg-align"),
                                    ),
                                    VListChild.Elem(elem = lowerGroup, shift = lowerShift),
                                ),
                        ),
                        options,
                    )
                } else {
                    makeVList(
                        VListParam.IndividualShift(
                            children =
                                listOf(
                                    VListChild.Elem(elem = upperGroup, shift = upperShift),
                                    VListChild.Elem(
                                        elem = arrowBody,
                                        shift = arrowShift,
                                        wrapperClasses = listOf("svg-align"),
                                    ),
                                ),
                        ),
                        options,
                    )
                }

            makeSpan(listOf("mrel", "x-arrow"), listOf(vlist), options)
        },
    )
}
