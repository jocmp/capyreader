package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlBuilder
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.SymbolNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunctionBuilders
import com.jocmp.mallet.tex.getTypeOfDomTree
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

internal fun defineSupSub() {
    defineFunctionBuilders(type = "supsub", htmlBuilder = ::supSubHtmlBuilder)
}

private fun supSubHtmlBuilder(node: ParseNode, options: Options): HtmlDomNode {
    val group = assertNodeType<ParseNode.SupSub>(node)

    val builderDelegate = htmlBuilderDelegate(group, options)
    if (builderDelegate != null) {
        return builderDelegate(group, options)
    }

    val valueBase = group.base
    val valueSup = group.sup
    val valueSub = group.sub
    val base = buildGroup(valueBase, options)
    var supm: HtmlDomNode? = null
    var subm: HtmlDomNode? = null

    val metrics = options.fontMetrics()

    var supShift = 0.0
    var subShift = 0.0

    val isCharBox = valueBase != null && isCharacterBox(valueBase)
    if (valueSup != null) {
        val newOptions = options.havingStyle(options.style.sup())
        supm = buildGroup(valueSup, newOptions, options)
        if (!isCharBox) {
            supShift = base.height - newOptions.fontMetrics().supDrop * newOptions.sizeMultiplier / options.sizeMultiplier
        }
    }

    if (valueSub != null) {
        val newOptions = options.havingStyle(options.style.sub())
        subm = buildGroup(valueSub, newOptions, options)
        if (!isCharBox) {
            subShift = base.depth + newOptions.fontMetrics().subDrop * newOptions.sizeMultiplier / options.sizeMultiplier
        }
    }

    val minSupShift =
        if (options.style === Style.DISPLAY) {
            metrics.sup1
        } else if (options.style.cramped) {
            metrics.sup3
        } else {
            metrics.sup2
        }

    val multiplier = options.sizeMultiplier
    val marginRight = makeEm((0.5 / metrics.ptPerEm) / multiplier)

    var marginLeft: String? = null
    if (subm != null) {
        val groupBase = group.base
        val isOiint =
            groupBase is ParseNode.Op && groupBase.name != null &&
                (groupBase.name == "\\oiint" || groupBase.name == "\\oiiint")
        if (base is SymbolNode || isOiint) {
            val italic =
                when (base) {
                    is SymbolNode -> base.italic
                    is Span -> base.italic ?: 0.0
                    else -> 0.0
                }
            marginLeft = makeEm(-italic)
        }
    }

    val supsub =
        if (supm != null && subm != null) {
            supShift = maxOf(supShift, minSupShift, supm.depth + 0.25 * metrics.xHeight)
            subShift = maxOf(subShift, metrics.sub2)

            val ruleWidth = metrics.defaultRuleThickness

            val maxWidth = 4 * ruleWidth
            if ((supShift - supm.depth) - (subm.height - subShift) < maxWidth) {
                subShift = maxWidth - (supShift - supm.depth) + subm.height
                val psi = 0.8 * metrics.xHeight - (supShift - supm.depth)
                if (psi > 0) {
                    supShift += psi
                    subShift -= psi
                }
            }

            val vlistElem =
                listOf(
                    VListChild.Elem(elem = subm, shift = subShift, marginRight = marginRight, marginLeft = marginLeft),
                    VListChild.Elem(elem = supm, shift = -supShift, marginRight = marginRight),
                )

            makeVList(VListParam.IndividualShift(vlistElem), options)
        } else if (subm != null) {
            subShift = maxOf(subShift, metrics.sub1, subm.height - 0.8 * metrics.xHeight)

            val vlistElem = listOf(VListChild.Elem(elem = subm, marginLeft = marginLeft, marginRight = marginRight))

            makeVList(VListParam.Shift(positionData = subShift, children = vlistElem), options)
        } else if (supm != null) {
            supShift = maxOf(supShift, minSupShift, supm.depth + 0.25 * metrics.xHeight)

            makeVList(
                VListParam.Shift(
                    positionData = -supShift,
                    children = listOf(VListChild.Elem(elem = supm, marginRight = marginRight)),
                ),
                options,
            )
        } else {
            throw IllegalStateException("supsub must have either sup or sub.")
        }

    val mclass = getTypeOfDomTree(base, "right") ?: "mord"
    return makeSpan(
        listOf(mclass),
        listOf(base, makeSpan(listOf("msupsub"), listOf(supsub))),
        options,
    )
}

/** The inner group's builder when it handles its own scripts instead of supsub. */
private fun htmlBuilderDelegate(group: ParseNode.SupSub, options: Options): HtmlBuilder? {
    val base = group.base
    return if (base == null) {
        null
    } else if (base is ParseNode.Op) {
        val delegate = base.limits && (options.style.size == Style.DISPLAY.size || base.alwaysHandleSupSub == true)
        if (delegate) ::opHtmlBuilder else null
    } else if (base is ParseNode.OperatorName) {
        val delegate = base.alwaysHandleSupSub && (options.style.size == Style.DISPLAY.size || base.limits)
        if (delegate) ::operatorNameHtmlBuilder else null
    } else if (base is ParseNode.Accent) {
        if (isCharacterBox(base.base)) ::accentHtmlBuilder else null
    } else if (base is ParseNode.HorizBrace) {
        val isSup = group.sub == null
        if (isSup == base.isOver) ::horizBraceHtmlBuilder else null
    } else {
        null
    }
}
