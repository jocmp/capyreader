package com.jocmp.mallet.tex

enum class Mode(val key: String) {
    MATH("math"),
    TEXT("text"),
}

data class Measurement(
    val number: Double,
    val unit: String,
)

sealed interface AlignSpec {
    data class Separator(val separator: String) : AlignSpec

    data class Align(
        val align: String,
        val pregap: Double? = null,
        val postgap: Double? = null,
    ) : AlignSpec
}

/**
 * KaTeX's parse nodes (types/nodes.ts). [type] keeps KaTeX's type string, which the builder
 * registry dispatches on. Fields are mutable where KaTeX mutates them after parsing.
 */
sealed class ParseNode(
    val type: String,
    val mode: Mode,
    override var loc: SourceLocation? = null,
) : Located {
    class Array(
        mode: Mode,
        var body: List<List<ParseNode>>,
        var rowGaps: List<Measurement?>,
        var hLinesBeforeRow: List<List<Boolean>>,
        var arraystretch: Double,
        var colSeparationType: String? = null,
        var hskipBeforeAndAfter: Boolean? = null,
        var addJot: Boolean? = null,
        var cols: List<AlignSpec>? = null,
        var tags: List<Any>? = null,
        var leqno: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("array", mode, loc)

    class CdLabel(mode: Mode, val side: String, val label: ParseNode, loc: SourceLocation? = null) :
        ParseNode("cdlabel", mode, loc)

    class CdLabelParent(mode: Mode, val fragment: ParseNode, loc: SourceLocation? = null) :
        ParseNode("cdlabelparent", mode, loc)

    class Color(mode: Mode, val color: String, val body: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("color", mode, loc)

    class ColorToken(mode: Mode, val color: String, loc: SourceLocation? = null) :
        ParseNode("color-token", mode, loc)

    /** Exactly one of [name] or [body] is set unless [symbol] is true, which requires [name]. */
    class Op(
        mode: Mode,
        var limits: Boolean,
        var parentIsSupSub: Boolean,
        val symbol: Boolean,
        val name: String? = null,
        val body: List<ParseNode>? = null,
        var alwaysHandleSupSub: Boolean? = null,
        var suppressBaseShift: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("op", mode, loc)

    class OrdGroup(
        mode: Mode,
        val body: List<ParseNode>,
        val semisimple: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("ordgroup", mode, loc)

    class Raw(mode: Mode, val string: String, loc: SourceLocation? = null) : ParseNode("raw", mode, loc)

    class Size(mode: Mode, val value: Measurement, val isBlank: Boolean, loc: SourceLocation? = null) :
        ParseNode("size", mode, loc)

    class Styling(
        mode: Mode,
        val style: String,
        val body: List<ParseNode>,
        val resetFont: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("styling", mode, loc)

    class SupSub(
        mode: Mode,
        val base: ParseNode?,
        val sup: ParseNode? = null,
        val sub: ParseNode? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("supsub", mode, loc)

    class Tag(mode: Mode, val body: List<ParseNode>, val tag: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("tag", mode, loc)

    class Text(mode: Mode, val body: List<ParseNode>, val font: String? = null, loc: SourceLocation? = null) :
        ParseNode("text", mode, loc)

    class Url(mode: Mode, val url: String, loc: SourceLocation? = null) : ParseNode("url", mode, loc)

    class Verb(mode: Mode, val body: String, val star: Boolean, loc: SourceLocation? = null) :
        ParseNode("verb", mode, loc)

    /** Single-symbol nodes: atom, mathord, textord, spacing, accent-token, op-token. */
    sealed class Symbol(type: String, mode: Mode, val text: String, loc: SourceLocation?) : ParseNode(type, mode, loc)

    class Atom(mode: Mode, val family: String, text: String, loc: SourceLocation? = null) :
        Symbol("atom", mode, text, loc)

    class MathOrd(mode: Mode, text: String, loc: SourceLocation? = null) : Symbol("mathord", mode, text, loc)

    class TextOrd(mode: Mode, text: String, loc: SourceLocation? = null) : Symbol("textord", mode, text, loc)

    class Spacing(mode: Mode, text: String, loc: SourceLocation? = null) : Symbol("spacing", mode, text, loc)

    class AccentToken(mode: Mode, text: String, loc: SourceLocation? = null) : Symbol("accent-token", mode, text, loc)

    class OpToken(mode: Mode, text: String, loc: SourceLocation? = null) : Symbol("op-token", mode, text, loc)

    class Accent(
        mode: Mode,
        val label: String,
        val base: ParseNode,
        val isStretchy: Boolean? = null,
        val isShifty: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("accent", mode, loc)

    class AccentUnder(
        mode: Mode,
        val label: String,
        val base: ParseNode,
        val isStretchy: Boolean? = null,
        val isShifty: Boolean? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("accentUnder", mode, loc)

    class Cr(mode: Mode, val newLine: Boolean, val size: Measurement?, loc: SourceLocation? = null) :
        ParseNode("cr", mode, loc)

    class DelimSizing(mode: Mode, val size: Int, val mclass: String, val delim: String, loc: SourceLocation? = null) :
        ParseNode("delimsizing", mode, loc)

    class Enclose(
        mode: Mode,
        val label: String,
        val body: ParseNode,
        val backgroundColor: String? = null,
        val borderColor: String? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("enclose", mode, loc)

    class Environment(mode: Mode, val name: String, val nameGroup: ParseNode, loc: SourceLocation? = null) :
        ParseNode("environment", mode, loc)

    class Font(mode: Mode, val font: String, val body: ParseNode, loc: SourceLocation? = null) :
        ParseNode("font", mode, loc)

    class GenFrac(
        mode: Mode,
        val continued: Boolean,
        val numer: ParseNode,
        val denom: ParseNode,
        val hasBarLine: Boolean,
        val leftDelim: String?,
        val rightDelim: String?,
        val barSize: Measurement?,
        loc: SourceLocation? = null,
    ) : ParseNode("genfrac", mode, loc)

    class HBox(mode: Mode, val body: List<ParseNode>, loc: SourceLocation? = null) : ParseNode("hbox", mode, loc)

    class HorizBrace(mode: Mode, val label: String, val isOver: Boolean, val base: ParseNode, loc: SourceLocation? = null) :
        ParseNode("horizBrace", mode, loc)

    class Href(mode: Mode, val href: String, val body: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("href", mode, loc)

    class Html(mode: Mode, val attributes: Map<String, String>, val body: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("html", mode, loc)

    class HtmlMathml(mode: Mode, val html: List<ParseNode>, val mathml: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("htmlmathml", mode, loc)

    class IncludeGraphics(
        mode: Mode,
        val alt: String,
        val width: Measurement,
        val height: Measurement,
        val totalheight: Measurement,
        val src: String,
        loc: SourceLocation? = null,
    ) : ParseNode("includegraphics", mode, loc)

    class Infix(
        mode: Mode,
        val replaceWith: String,
        val size: Measurement? = null,
        val token: Token? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("infix", mode, loc)

    class Internal(mode: Mode, loc: SourceLocation? = null) : ParseNode("internal", mode, loc)

    class Kern(mode: Mode, val dimension: Measurement, loc: SourceLocation? = null) : ParseNode("kern", mode, loc)

    class Lap(mode: Mode, val alignment: String, val body: ParseNode, loc: SourceLocation? = null) :
        ParseNode("lap", mode, loc)

    class LeftRight(
        mode: Mode,
        val body: List<ParseNode>,
        val left: String,
        val right: String,
        val rightColor: String? = null,
        loc: SourceLocation? = null,
    ) : ParseNode("leftright", mode, loc)

    class LeftRightRight(mode: Mode, val delim: String, val color: String? = null, loc: SourceLocation? = null) :
        ParseNode("leftright-right", mode, loc)

    class MathChoice(
        mode: Mode,
        val display: List<ParseNode>,
        val text: List<ParseNode>,
        val script: List<ParseNode>,
        val scriptscript: List<ParseNode>,
        loc: SourceLocation? = null,
    ) : ParseNode("mathchoice", mode, loc)

    class Middle(mode: Mode, val delim: String, loc: SourceLocation? = null) : ParseNode("middle", mode, loc)

    class MClass(
        mode: Mode,
        val mclass: String,
        val body: List<ParseNode>,
        val isCharacterBox: Boolean,
        loc: SourceLocation? = null,
    ) : ParseNode("mclass", mode, loc)

    class OperatorName(
        mode: Mode,
        val body: List<ParseNode>,
        var alwaysHandleSupSub: Boolean,
        var limits: Boolean,
        var parentIsSupSub: Boolean,
        loc: SourceLocation? = null,
    ) : ParseNode("operatorname", mode, loc)

    class Overline(mode: Mode, val body: ParseNode, loc: SourceLocation? = null) : ParseNode("overline", mode, loc)

    class Underline(mode: Mode, val body: ParseNode, loc: SourceLocation? = null) : ParseNode("underline", mode, loc)

    class Phantom(mode: Mode, val body: List<ParseNode>, loc: SourceLocation? = null) : ParseNode("phantom", mode, loc)

    class VPhantom(mode: Mode, val body: ParseNode, loc: SourceLocation? = null) : ParseNode("vphantom", mode, loc)

    class Pmb(mode: Mode, val mclass: String, val body: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("pmb", mode, loc)

    class RaiseBox(mode: Mode, val dy: Measurement, val body: ParseNode, loc: SourceLocation? = null) :
        ParseNode("raisebox", mode, loc)

    class Rule(
        mode: Mode,
        val shift: Measurement?,
        val width: Measurement,
        val height: Measurement,
        loc: SourceLocation? = null,
    ) : ParseNode("rule", mode, loc)

    class Sizing(mode: Mode, val size: Int, val body: List<ParseNode>, loc: SourceLocation? = null) :
        ParseNode("sizing", mode, loc)

    class Smash(
        mode: Mode,
        val body: ParseNode,
        val smashHeight: Boolean,
        val smashDepth: Boolean,
        loc: SourceLocation? = null,
    ) : ParseNode("smash", mode, loc)

    class Sqrt(mode: Mode, val body: ParseNode, val index: ParseNode?, loc: SourceLocation? = null) :
        ParseNode("sqrt", mode, loc)

    class VCenter(mode: Mode, val body: ParseNode, loc: SourceLocation? = null) : ParseNode("vcenter", mode, loc)

    class XArrow(mode: Mode, val label: String, val body: ParseNode, val below: ParseNode?, loc: SourceLocation? = null) :
        ParseNode("xArrow", mode, loc)

    class ReflectBox(mode: Mode, val body: ParseNode, loc: SourceLocation? = null) :
        ParseNode("reflectbox", mode, loc)
}

private val atoms = setOf("bin", "close", "inner", "open", "punct", "rel")

fun isAtom(group: String): Boolean = group in atoms

fun checkSymbolNodeType(node: ParseNode?): ParseNode.Symbol? = node as? ParseNode.Symbol

fun assertSymbolNodeType(node: ParseNode?): ParseNode.Symbol =
    node as? ParseNode.Symbol ?: throw IllegalStateException("Expected node of symbol group type, but got ${node?.type}")

inline fun <reified T : ParseNode> assertNodeType(node: ParseNode?): T =
    node as? T ?: throw IllegalStateException("Expected node of type ${T::class.simpleName}, but got ${node?.type}")

fun assertCharacterGroup(group: ParseNode.OrdGroup, errorMessage: String, allowSpaces: Boolean = false): String =
    group.body.joinToString("") { node ->
        when {
            node is ParseNode.TextOrd -> node.text
            allowSpaces && node is ParseNode.Spacing && node.text == " " -> " "
            else -> throw ParseError(errorMessage, group)
        }
    }

fun normalizeArgument(arg: ParseNode): ParseNode =
    if (arg is ParseNode.OrdGroup && arg.body.size == 1) arg.body[0] else arg

fun ordargument(arg: ParseNode): List<ParseNode> = if (arg is ParseNode.OrdGroup) arg.body else listOf(arg)

/** The symbol node for a symbol-table group, as `Parser.parseSymbol` builds it. */
fun symbolNode(group: String, mode: Mode, text: String, loc: SourceLocation? = null): ParseNode.Symbol =
    when (group) {
        "mathord" -> ParseNode.MathOrd(mode, text, loc)
        "textord" -> ParseNode.TextOrd(mode, text, loc)
        "spacing" -> ParseNode.Spacing(mode, text, loc)
        "accent-token" -> ParseNode.AccentToken(mode, text, loc)
        "op-token" -> ParseNode.OpToken(mode, text, loc)
        else -> ParseNode.Atom(mode, group, text, loc)
    }
