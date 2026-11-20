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

internal fun defineUnderline() {
    defineFunction(
        type = "underline",
        names = listOf("\\underline"),
        numArgs = 1,
        allowedInText = true,
        handler = { context, args, _ ->
            ParseNode.Underline(
                mode = context.parser.mode,
                body = args[0],
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.Underline>(node)

            val innerGroup = buildGroup(group.body, options)

            val line = makeLineSpan("underline-line", options)

            val defaultRuleThickness = options.fontMetrics().defaultRuleThickness
            val vlist =
                makeVList(
                    VListParam.Top(
                        positionData = innerGroup.height,
                        children =
                            listOf(
                                VListChild.Kern(defaultRuleThickness),
                                VListChild.Elem(elem = line),
                                VListChild.Kern(3 * defaultRuleThickness),
                                VListChild.Elem(elem = innerGroup),
                            ),
                    ),
                    options,
                )

            makeSpan(listOf("mord", "katex-underline"), listOf(vlist), options)
        },
    )
}
