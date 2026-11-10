package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeLineSpan
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

internal fun defineOverline() {
    defineFunction(
        type = "overline",
        names = listOf("\\overline"),
        numArgs = 1,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Overline(
                mode = context.parser.mode,
                body = body,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.Overline>(node)

            val innerGroup = buildGroup(group.body, options.havingCrampedStyle())

            val line = makeLineSpan("overline-line", options)

            val defaultRuleThickness = options.fontMetrics().defaultRuleThickness
            val vlist =
                makeVList(
                    VListParam.FirstBaseline(
                        children =
                            listOf(
                                VListChild.Elem(elem = innerGroup),
                                VListChild.Kern(3 * defaultRuleThickness),
                                VListChild.Elem(elem = line),
                                VListChild.Kern(defaultRuleThickness),
                            ),
                    ),
                    options,
                )

            makeSpan(listOf("mord", "katex-overline"), listOf(vlist), options)
        },
    )
}
