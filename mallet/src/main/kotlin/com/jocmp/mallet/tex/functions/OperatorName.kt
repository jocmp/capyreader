package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.SymbolNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

internal fun defineOperatorName() {
    defineFunction(
        type = "operatorname",
        names = listOf("\\operatorname@", "\\operatornamewithlimits"),
        numArgs = 1,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.OperatorName(
                mode = context.parser.mode,
                body = ordargument(body),
                alwaysHandleSupSub = context.funcName == "\\operatornamewithlimits",
                limits = false,
                parentIsSupSub = false,
            )
        },
        htmlBuilder = ::operatorNameHtmlBuilder,
    )
}

/** Builds an operatorname, or a supsub whose base is one that handles its own limits. */
internal fun operatorNameHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    var supGroup: ParseNode? = null
    var subGroup: ParseNode? = null
    var hasLimits = false
    val operatorName: ParseNode.OperatorName
    if (group is ParseNode.SupSub) {
        supGroup = group.sup
        subGroup = group.sub
        operatorName = assertNodeType<ParseNode.OperatorName>(group.base)
        hasLimits = true
    } else {
        operatorName = assertNodeType<ParseNode.OperatorName>(group)
    }

    val base =
        if (operatorName.body.isNotEmpty()) {
            val body =
                operatorName.body.map { child ->
                    if (child is ParseNode.Symbol) {
                        ParseNode.TextOrd(mode = child.mode, text = child.text)
                    } else {
                        child
                    }
                }

            val expression = buildExpression(body, options.withFont("mathrm"), true)

            expression.forEach { child ->
                if (child is SymbolNode) {
                    child.text = child.text.replaceFirst("−", "-").replaceFirst("∗", "*")
                }
            }
            makeSpan(listOf("mop"), expression, options)
        } else {
            makeSpan(listOf("mop"), emptyList(), options)
        }

    return if (hasLimits) {
        assembleSupSub(base, supGroup, subGroup, options, options.style, 0.0, 0.0)
    } else {
        base
    }
}
