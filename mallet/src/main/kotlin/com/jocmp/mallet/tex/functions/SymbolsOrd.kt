package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunctionBuilders
import com.jocmp.mallet.tex.makeOrd

internal fun defineSymbolsOrd() {
    defineFunctionBuilders(type = "mathord") { node, options ->
        makeOrd(assertNodeType<ParseNode.MathOrd>(node), options)
    }

    defineFunctionBuilders(type = "textord") { node, options ->
        makeOrd(assertNodeType<ParseNode.TextOrd>(node), options)
    }
}
