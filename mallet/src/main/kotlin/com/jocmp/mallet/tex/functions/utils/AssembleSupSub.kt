package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Style
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

/** For an operator with limits, assembles the base, sup, and sub into a span. */
internal fun assembleSupSub(
    base: HtmlDomNode,
    supGroup: ParseNode?,
    subGroup: ParseNode?,
    options: Options,
    style: Style,
    slant: Double,
    baseShift: Double,
): Span {
    val wrappedBase = makeSpan(emptyList(), listOf(base))
    val subIsSingleCharacter = subGroup != null && isCharacterBox(subGroup)
    var sub: Limit? = null
    var sup: Limit? = null

    if (supGroup != null) {
        val elem = buildGroup(supGroup, options.havingStyle(style.sup()), options)

        sup =
            Limit(
                elem = elem,
                kern =
                    maxOf(
                        options.fontMetrics().bigOpSpacing1,
                        options.fontMetrics().bigOpSpacing3 - elem.depth,
                    ),
            )
    }

    if (subGroup != null) {
        val elem = buildGroup(subGroup, options.havingStyle(style.sub()), options)

        sub =
            Limit(
                elem = elem,
                kern =
                    maxOf(
                        options.fontMetrics().bigOpSpacing2,
                        options.fontMetrics().bigOpSpacing4 - elem.height,
                    ),
            )
    }

    val finalGroup =
        if (sup != null && sub != null) {
            val bottom =
                options.fontMetrics().bigOpSpacing5 +
                    sub.elem.height + sub.elem.depth +
                    sub.kern +
                    wrappedBase.depth + baseShift

            makeVList(
                VListParam.Bottom(
                    positionData = bottom,
                    children =
                        listOf(
                            VListChild.Kern(options.fontMetrics().bigOpSpacing5),
                            VListChild.Elem(elem = sub.elem, marginLeft = makeEm(-slant)),
                            VListChild.Kern(sub.kern),
                            VListChild.Elem(elem = wrappedBase),
                            VListChild.Kern(sup.kern),
                            VListChild.Elem(elem = sup.elem, marginLeft = makeEm(slant)),
                            VListChild.Kern(options.fontMetrics().bigOpSpacing5),
                        ),
                ),
                options,
            )
        } else if (sub != null) {
            val top = wrappedBase.height - baseShift

            makeVList(
                VListParam.Top(
                    positionData = top,
                    children =
                        listOf(
                            VListChild.Kern(options.fontMetrics().bigOpSpacing5),
                            VListChild.Elem(elem = sub.elem, marginLeft = makeEm(-slant)),
                            VListChild.Kern(sub.kern),
                            VListChild.Elem(elem = wrappedBase),
                        ),
                ),
                options,
            )
        } else if (sup != null) {
            val bottom = wrappedBase.depth + baseShift

            makeVList(
                VListParam.Bottom(
                    positionData = bottom,
                    children =
                        listOf(
                            VListChild.Elem(elem = wrappedBase),
                            VListChild.Kern(sup.kern),
                            VListChild.Elem(elem = sup.elem, marginLeft = makeEm(slant)),
                            VListChild.Kern(options.fontMetrics().bigOpSpacing5),
                        ),
                ),
                options,
            )
        } else {
            return wrappedBase
        }

    val parts = mutableListOf<HtmlDomNode>(finalGroup)
    if (sub != null && slant != 0.0 && !subIsSingleCharacter) {
        val spacer = makeSpan(listOf("mspace"), emptyList(), options)
        spacer.style.marginRight = makeEm(slant)
        parts.add(0, spacer)
    }
    return makeSpan(listOf("mop", "op-limits"), parts, options)
}

private class Limit(
    val elem: HtmlDomNode,
    val kern: Double,
)
