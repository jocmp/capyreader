package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.CssStyle
import com.jocmp.mallet.tex.DocumentFragment
import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Span
import com.jocmp.mallet.tex.Stretchy
import com.jocmp.mallet.tex.SymbolNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.VirtualNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.isCharacterBox
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeOrd
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList
import com.jocmp.mallet.tex.normalizeArgument
import com.jocmp.mallet.tex.staticSvg
import com.jocmp.mallet.tex.svgData

private val NON_STRETCHY_ACCENT_REGEX =
    Regex(
        listOf(
            "\\acute", "\\grave", "\\ddot", "\\tilde", "\\bar", "\\breve",
            "\\check", "\\hat", "\\vec", "\\dot", "\\mathring",
        ).joinToString("|") { Regex.escape(it) },
    )

internal fun defineAccent() {
    defineFunction(
        type = "accent",
        names =
            listOf(
                "\\acute", "\\grave", "\\ddot", "\\tilde", "\\bar", "\\breve",
                "\\check", "\\hat", "\\vec", "\\dot", "\\mathring", "\\widecheck",
                "\\widehat", "\\widetilde", "\\overrightarrow", "\\overleftarrow",
                "\\Overrightarrow", "\\overleftrightarrow", "\\overgroup",
                "\\overlinesegment", "\\overleftharpoon", "\\overrightharpoon",
            ),
        numArgs = 1,
        handler = { context, args, _ ->
            val base = normalizeArgument(args[0])

            val isStretchy = !NON_STRETCHY_ACCENT_REGEX.containsMatchIn(context.funcName)
            val isShifty =
                !isStretchy ||
                    context.funcName == "\\widehat" ||
                    context.funcName == "\\widetilde" ||
                    context.funcName == "\\widecheck"

            ParseNode.Accent(
                mode = context.parser.mode,
                label = context.funcName,
                isStretchy = isStretchy,
                isShifty = isShifty,
                base = base,
            )
        },
        htmlBuilder = ::accentHtmlBuilder,
    )

    defineFunction(
        type = "accent",
        names =
            listOf(
                "\\'", "\\`", "\\^", "\\~", "\\=", "\\u", "\\.", "\\\"",
                "\\c", "\\r", "\\H", "\\v", "\\textcircled",
            ),
        numArgs = 1,
        allowedInText = true,
        allowedInMath = true,
        argTypes = listOf(ArgType.PRIMITIVE),
        handler = { context, args, _ ->
            val base = args[0]
            var mode = context.parser.mode

            if (mode == Mode.MATH) {
                context.parser.settings.reportNonstrict(
                    "mathVsTextAccents",
                    "LaTeX's accent ${context.funcName} works only in text mode",
                )
                mode = Mode.TEXT
            }

            ParseNode.Accent(
                mode = mode,
                label = context.funcName,
                isStretchy = false,
                isShifty = true,
                base = base,
            )
        },
    )
}

internal fun accentHtmlBuilder(group: ParseNode, options: Options): HtmlDomNode {
    val grp = group
    val base: ParseNode
    val accentGroup: ParseNode.Accent

    var supSubGroup: Span? = null
    if (grp is ParseNode.SupSub) {
        accentGroup = assertNodeType<ParseNode.Accent>(grp.base)
        base = accentGroup.base
        // ParseNode.SupSub.base is a val, so render a copy with the new base instead of mutating grp.
        val rebased = ParseNode.SupSub(mode = grp.mode, base = base, sup = grp.sup, sub = grp.sub, loc = grp.loc)

        supSubGroup = buildGroup(rebased, options) as Span
    } else {
        accentGroup = assertNodeType<ParseNode.Accent>(grp)
        base = accentGroup.base
    }

    val body = buildGroup(base, options.havingCrampedStyle())

    val mustShift = accentGroup.isShifty == true && isCharacterBox(base)

    var skew = 0.0
    if (mustShift) {
        skew = getBaseSymbol(body)?.skew ?: 0.0
    }

    val accentBelow = accentGroup.label == "\\c"

    var clearance =
        if (accentBelow) {
            body.height + body.depth
        } else {
            minOf(body.height, options.fontMetrics().xHeight)
        }

    val accentBody: HtmlDomNode
    if (accentGroup.isStretchy != true) {
        val accent: HtmlDomNode
        val width: Double
        if (accentGroup.label == "\\vec") {
            accent = staticSvg("vec", options)
            width = svgData.getValue("vec").width
        } else {
            val symbol = makeOrd(ParseNode.TextOrd(mode = accentGroup.mode, text = accentGroup.label), options) as SymbolNode
            symbol.italic = 0.0
            width = symbol.width
            if (accentBelow) {
                clearance += symbol.depth
            }
            accent = symbol
        }

        val accentSpan = makeSpan(listOf("accent-body"), listOf(accent))

        val accentFull = accentGroup.label == "\\textcircled"
        if (accentFull) {
            accentSpan.classes.add("accent-full")
            clearance = body.height
        }

        var left = skew

        if (!accentFull) {
            left -= width / 2
        }

        accentSpan.style.left = makeEm(left)

        if (accentGroup.label == "\\textcircled") {
            accentSpan.style.top = ".2em"
        }

        accentBody =
            makeVList(
                VListParam.FirstBaseline(
                    children =
                        listOf(
                            VListChild.Elem(elem = body),
                            VListChild.Kern(-clearance),
                            VListChild.Elem(elem = accentSpan),
                        ),
                ),
                options,
            )
    } else {
        val stretchy = Stretchy.stretchySvg(accentGroup, options)

        accentBody =
            makeVList(
                VListParam.FirstBaseline(
                    children =
                        listOf(
                            VListChild.Elem(elem = body),
                            VListChild.Elem(
                                elem = stretchy,
                                wrapperClasses = listOf("svg-align"),
                                wrapperStyle =
                                    if (skew > 0) {
                                        CssStyle(
                                            width = "calc(100% - ${makeEm(2 * skew)})",
                                            marginLeft = makeEm(2 * skew),
                                        )
                                    } else {
                                        null
                                    },
                            ),
                        ),
                ),
                options,
            )
    }

    val accentWrap = makeSpan(listOf("mord", "katex-accent"), listOf(accentBody), options)

    if (supSubGroup != null) {
        supSubGroup.children[0] = accentWrap

        supSubGroup.height = maxOf(accentWrap.height, supSubGroup.height)

        supSubGroup.classes[0] = "mord"

        return supSubGroup
    } else {
        return accentWrap
    }
}

private fun getBaseSymbol(group: VirtualNode): SymbolNode? {
    if (group is SymbolNode) {
        return group
    }
    if (group is Span && group.children.size == 1) {
        return getBaseSymbol(group.children[0])
    }
    if (group is DocumentFragment && group.children.size == 1) {
        return getBaseSymbol(group.children[0])
    }
    return null
}
