package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeVList

internal fun defineRaiseBox() {
    defineFunction(
        type = "raisebox",
        names = listOf("\\raisebox"),
        numArgs = 2,
        argTypes = listOf(ArgType.SIZE, ArgType.HBOX),
        allowedInText = true,
        handler = { context, args, _ ->
            val amount = assertNodeType<ParseNode.Size>(args[0]).value
            val body = args[1]
            ParseNode.RaiseBox(
                mode = context.parser.mode,
                dy = amount,
                body = body,
            )
        },
        htmlBuilder = { group, options ->
            val raisebox = assertNodeType<ParseNode.RaiseBox>(group)
            val body = buildGroup(raisebox.body, options)
            val dy = calculateSize(raisebox.dy, options)
            makeVList(
                VListParam.Shift(positionData = -dy, children = listOf(VListChild.Elem(elem = body))),
                options,
            )
        },
    )
}
