package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

internal fun defineMClass() {
    defineFunction(
        type = "mclass",
        names =
            listOf(
                "\\mathord", "\\mathbin", "\\mathrel", "\\mathopen",
                "\\mathclose", "\\mathpunct", "\\mathinner",
            ),
        numArgs = 1,
        primitive = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.MClass(
                mode = context.parser.mode,
                mclass = "m${context.funcName.substring(5)}",
                body = ordargument(body),
                isCharacterBox = isCharacterBox(body),
            )
        },
        htmlBuilder = ::mclassHtmlBuilder,
    )

    defineFunction(
        type = "mclass",
        names = listOf("\\@binrel"),
        numArgs = 2,
        handler = { context, args, _ ->
            ParseNode.MClass(
                mode = context.parser.mode,
                mclass = binrelClass(args[0]),
                body = ordargument(args[1]),
                isCharacterBox = isCharacterBox(args[1]),
            )
        },
    )

    defineFunction(
        type = "mclass",
        names = listOf("\\stackrel", "\\overset", "\\underset"),
        numArgs = 2,
        handler = { context, args, _ ->
            val funcName = context.funcName
            val baseArg = args[1]
            val shiftedArg = args[0]

            val mclass =
                if (funcName != "\\stackrel") {
                    binrelClass(baseArg)
                } else {
                    "mrel"
                }

            val baseOp =
                ParseNode.Op(
                    mode = baseArg.mode,
                    limits = true,
                    alwaysHandleSupSub = true,
                    parentIsSupSub = false,
                    symbol = false,
                    suppressBaseShift = funcName != "\\stackrel",
                    body = ordargument(baseArg),
                )

            val supsub =
                if (funcName == "\\underset") {
                    ParseNode.SupSub(mode = shiftedArg.mode, base = baseOp, sub = shiftedArg)
                } else {
                    ParseNode.SupSub(mode = shiftedArg.mode, base = baseOp, sup = shiftedArg)
                }

            ParseNode.MClass(
                mode = context.parser.mode,
                mclass = mclass,
                body = listOf(supsub),
                isCharacterBox = isCharacterBox(supsub),
            )
        },
    )
}

/** \binrel@ spacing: mbin/mrel when the argument's atom is bin/rel, otherwise mord. */
internal fun binrelClass(arg: ParseNode): String {
    val atom = if (arg is ParseNode.OrdGroup && arg.body.isNotEmpty()) arg.body[0] else arg
    return if (atom is ParseNode.Atom && (atom.family == "bin" || atom.family == "rel")) {
        "m${atom.family}"
    } else {
        "mord"
    }
}

private fun mclassHtmlBuilder(node: ParseNode, options: Options): HtmlDomNode {
    val group = assertNodeType<ParseNode.MClass>(node)
    val elements = buildExpression(group.body, options, true)
    return makeSpan(listOf(group.mclass), elements, options)
}
