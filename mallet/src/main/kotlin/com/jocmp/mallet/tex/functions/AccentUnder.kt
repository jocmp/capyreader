package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Stretchy
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

internal fun defineAccentUnder() {
    defineFunction(
        type = "accentUnder",
        names =
            listOf(
                "\\underleftarrow", "\\underrightarrow", "\\underleftrightarrow",
                "\\undergroup", "\\underlinesegment", "\\utilde",
            ),
        numArgs = 1,
        handler = { context, args, _ ->
            val base = args[0]
            ParseNode.AccentUnder(
                mode = context.parser.mode,
                label = context.funcName,
                base = base,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.AccentUnder>(node)
            val innerGroup = buildGroup(group.base, options)

            val accentBody = Stretchy.stretchySvg(group, options)
            val kern = if (group.label == "\\utilde") 0.12 else 0.0

            val vlist =
                makeVList(
                    VListParam.Top(
                        positionData = innerGroup.height,
                        children =
                            listOf(
                                VListChild.Elem(elem = accentBody, wrapperClasses = listOf("svg-align")),
                                VListChild.Kern(kern),
                                VListChild.Elem(elem = innerGroup),
                            ),
                    ),
                    options,
                )

            makeSpan(listOf("mord", "accentunder"), listOf(vlist), options)
        },
    )
}
