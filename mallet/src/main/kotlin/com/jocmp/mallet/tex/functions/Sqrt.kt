package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.Delimiter
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.wrapFragment

internal fun defineSqrt() {
    defineFunction(
        type = "sqrt",
        names = listOf("\\sqrt"),
        numArgs = 1,
        numOptionalArgs = 1,
        handler = { context, args, optArgs ->
            val index = optArgs[0]
            val body = args[0]
            ParseNode.Sqrt(
                mode = context.parser.mode,
                body = body,
                index = index,
            )
        },
        htmlBuilder = { node, options ->
            val group = assertNodeType<ParseNode.Sqrt>(node)

            var inner = buildGroup(group.body, options.havingCrampedStyle())
            if (inner.height == 0.0) {
                inner.height = options.fontMetrics().xHeight
            }

            inner = wrapFragment(inner, options)

            val metrics = options.fontMetrics()
            val theta = metrics.defaultRuleThickness

            var phi = theta
            if (options.style.id < Style.TEXT.id) {
                phi = options.fontMetrics().xHeight
            }

            var lineClearance = theta + phi / 4

            val minDelimiterHeight = inner.height + inner.depth + lineClearance + theta

            val sqrtImage = Delimiter.makeSqrtImage(minDelimiterHeight, options)
            val img = sqrtImage.span
            val ruleWidth = sqrtImage.ruleWidth
            val advanceWidth = sqrtImage.advanceWidth

            val delimDepth = img.height - ruleWidth

            if (delimDepth > inner.height + inner.depth + lineClearance) {
                lineClearance = (lineClearance + delimDepth - inner.height - inner.depth) / 2
            }

            val imgShift = img.height - inner.height - lineClearance - ruleWidth

            inner.style.paddingLeft = makeEm(advanceWidth)

            val body =
                makeVList(
                    VListParam.FirstBaseline(
                        children =
                            listOf(
                                VListChild.Elem(elem = inner, wrapperClasses = listOf("svg-align")),
                                VListChild.Kern(-(inner.height + imgShift)),
                                VListChild.Elem(elem = img),
                                VListChild.Kern(ruleWidth),
                            ),
                    ),
                    options,
                )

            if (group.index == null) {
                makeSpan(listOf("mord", "sqrt"), listOf(body), options)
            } else {
                val newOptions = options.havingStyle(Style.SCRIPTSCRIPT)
                val rootm = buildGroup(group.index, newOptions, options)

                val toShift = 0.6 * (body.height - body.depth)

                val rootVList =
                    makeVList(
                        VListParam.Shift(
                            positionData = -toShift,
                            children = listOf(VListChild.Elem(elem = rootm)),
                        ),
                        options,
                    )
                val rootVListWrap = makeSpan(listOf("katex-root"), listOf(rootVList))

                makeSpan(listOf("mord", "sqrt"), listOf(rootVListWrap, body), options)
            }
        },
    )
}
