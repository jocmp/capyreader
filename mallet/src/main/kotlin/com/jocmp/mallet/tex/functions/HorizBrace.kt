package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Stretchy
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

internal fun defineHorizBrace() {
    defineFunction(
        type = "horizBrace",
        names = listOf("\\overbrace", "\\underbrace", "\\overbracket", "\\underbracket"),
        numArgs = 1,
        handler = { context, args, _ ->
            ParseNode.HorizBrace(
                mode = context.parser.mode,
                label = context.funcName,
                isOver = context.funcName.contains("\\over"),
                base = args[0],
            )
        },
        htmlBuilder = ::horizBraceHtmlBuilder,
    )
}

internal fun horizBraceHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    val grp = group
    val style = options.style

    var supSubGroup: HtmlDomNode? = null
    val braceGroup: ParseNode.HorizBrace
    if (grp is ParseNode.SupSub) {
        supSubGroup =
            if (grp.sup != null) {
                buildGroup(grp.sup, options.havingStyle(style.sup()), options)
            } else {
                buildGroup(grp.sub, options.havingStyle(style.sub()), options)
            }
        braceGroup = assertNodeType<ParseNode.HorizBrace>(grp.base)
    } else {
        braceGroup = assertNodeType<ParseNode.HorizBrace>(grp)
    }

    val body = buildGroup(braceGroup.base, options.havingBaseStyle(Style.DISPLAY))

    val braceBody = Stretchy.stretchySvg(braceGroup, options)

    var vlist: HtmlDomNode =
        if (braceGroup.isOver) {
            makeVList(
                VListParam.FirstBaseline(
                    children =
                        listOf(
                            VListChild.Elem(elem = body),
                            VListChild.Kern(0.1),
                            VListChild.Elem(elem = braceBody, wrapperClasses = listOf("svg-align")),
                        ),
                ),
                options,
            )
        } else {
            makeVList(
                VListParam.Bottom(
                    positionData = body.depth + 0.1 + braceBody.height,
                    children =
                        listOf(
                            VListChild.Elem(elem = braceBody, wrapperClasses = listOf("svg-align")),
                            VListChild.Kern(0.1),
                            VListChild.Elem(elem = body),
                        ),
                ),
                options,
            )
        }

    if (supSubGroup != null) {
        val vSpan =
            makeSpan(
                listOf("minner", if (braceGroup.isOver) "mover" else "munder"),
                listOf(vlist),
                options,
            )

        vlist =
            if (braceGroup.isOver) {
                makeVList(
                    VListParam.FirstBaseline(
                        children =
                            listOf(
                                VListChild.Elem(elem = vSpan),
                                VListChild.Kern(0.2),
                                VListChild.Elem(elem = supSubGroup),
                            ),
                    ),
                    options,
                )
            } else {
                makeVList(
                    VListParam.Bottom(
                        positionData = vSpan.depth + 0.2 + supSubGroup.height + supSubGroup.depth,
                        children =
                            listOf(
                                VListChild.Elem(elem = supSubGroup),
                                VListChild.Kern(0.2),
                                VListChild.Elem(elem = vSpan),
                            ),
                    ),
                    options,
                )
            }
    }

    return makeSpan(
        listOf("minner", if (braceGroup.isOver) "mover" else "munder"),
        listOf(vlist),
        options,
    )
}
