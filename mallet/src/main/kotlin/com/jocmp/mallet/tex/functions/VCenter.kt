package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeVList

internal fun defineVCenter() {
    defineFunction(
        type = "vcenter",
        names = listOf("\\vcenter"),
        numArgs = 1,
        argTypes = listOf(ArgType.ORIGINAL),
        allowedInText = false,
        handler = { context, args, _ ->
            ParseNode.VCenter(
                mode = context.parser.mode,
                body = args[0],
            )
        },
        htmlBuilder = { group, options ->
            val vcenter = assertNodeType<ParseNode.VCenter>(group)
            val body = buildGroup(vcenter.body, options)
            val axisHeight = options.fontMetrics().axisHeight
            val dy = 0.5 * ((body.height - axisHeight) - (body.depth + axisHeight))
            makeVList(
                VListParam.Shift(positionData = dy, children = listOf(VListChild.Elem(elem = body))),
                options,
            )
        },
    )
}
