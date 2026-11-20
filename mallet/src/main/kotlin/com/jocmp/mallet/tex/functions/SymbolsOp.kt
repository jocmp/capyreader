package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunctionBuilders
import com.jocmp.mallet.tex.mathsym

internal fun defineSymbolsOp() {
    defineFunctionBuilders(type = "atom") { node, options ->
        val group = assertNodeType<ParseNode.Atom>(node)
        mathsym(group.text, group.mode, options, listOf("m" + group.family))
    }
}
