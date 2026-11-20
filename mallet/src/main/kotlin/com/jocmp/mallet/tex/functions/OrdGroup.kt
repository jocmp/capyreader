package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunctionBuilders
import com.jocmp.mallet.tex.makeFragment
import com.jocmp.mallet.tex.makeSpan

internal fun defineOrdGroup() {
    defineFunctionBuilders(type = "ordgroup") { node, options ->
        val group = assertNodeType<ParseNode.OrdGroup>(node)
        if (group.semisimple == true) {
            makeFragment(buildExpression(group.body, options, false))
        } else {
            makeSpan(listOf("mord"), buildExpression(group.body, options, true), options)
        }
    }
}
