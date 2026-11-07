package com.jocmp.mallet.math

/**
 * TeX atom types. Order matters: types before [BOUNDARY] accept sub- and superscripts.
 */
internal enum class AtomType {
    ORDINARY,
    NUMBER,
    VARIABLE,
    LARGE_OPERATOR,
    BINARY_OPERATOR,
    UNARY_OPERATOR,
    RELATION,
    OPEN,
    CLOSE,
    FRACTION,
    RADICAL,
    PUNCTUATION,
    INNER,
    UNDERLINE,
    OVERLINE,
    ACCENT,
    BOUNDARY,
    SPACE,
    STYLE,
    COLOR,
    TABLE,
    ;

    val allowsScripts: Boolean
        get() = this < BOUNDARY
}

internal enum class FontStyle {
    DEFAULT,
    ROMAN,
    BOLD,
    CALIGRAPHIC,
    TYPEWRITER,
    ITALIC,
    SANS_SERIF,
    FRAKTUR,
    BLACKBOARD,
    BOLD_ITALIC,
}

internal enum class LineStyle {
    DISPLAY,
    TEXT,
    SCRIPT,
    SCRIPT_SCRIPT,
}

internal enum class ColumnAlignment {
    LEFT,
    CENTER,
    RIGHT,
}

internal typealias MathList = MutableList<MathAtom>

internal open class MathAtom(
    var type: AtomType,
    var nucleus: String,
) {
    var superscript: MathList? = null
        set(value) {
            check(type.allowsScripts) { "Superscripts not allowed for $type" }
            field = value
        }

    var subscript: MathList? = null
        set(value) {
            check(type.allowsScripts) { "Subscripts not allowed for $type" }
            field = value
        }

    var fontStyle: FontStyle = FontStyle.DEFAULT

    val hasScripts: Boolean
        get() = superscript != null || subscript != null
}

internal class Fraction(
    var numerator: MathList = mutableListOf(),
    var denominator: MathList = mutableListOf(),
    var hasRule: Boolean = true,
    var leftDelimiter: String? = null,
    var rightDelimiter: String? = null,
) : MathAtom(AtomType.FRACTION, "")

internal class Radical(
    var radicand: MathList = mutableListOf(),
    var degree: MathList? = null,
) : MathAtom(AtomType.RADICAL, "")

internal class LargeOperator(
    nucleus: String,
    var hasLimits: Boolean,
) : MathAtom(AtomType.LARGE_OPERATOR, nucleus)

/**
 * A sub-list, optionally wrapped in `\left` and `\right` delimiters. The delimiters hold the
 * delimiter character, or an empty string for `.`.
 */
internal class Inner(
    var innerList: MathList = mutableListOf(),
    var leftBoundary: String? = null,
    var rightBoundary: String? = null,
) : MathAtom(AtomType.INNER, "")

internal class Overline(
    var innerList: MathList,
) : MathAtom(AtomType.OVERLINE, "")

internal class Underline(
    var innerList: MathList,
) : MathAtom(AtomType.UNDERLINE, "")

internal class Accent(
    nucleus: String,
    var innerList: MathList = mutableListOf(),
) : MathAtom(AtomType.ACCENT, nucleus)

/**
 * Space in mu (1/18 em).
 */
internal class MathSpace(
    val space: Float,
) : MathAtom(AtomType.SPACE, "")

internal class MathStyle(
    val style: LineStyle,
) : MathAtom(AtomType.STYLE, "")

internal class MathColor(
    val argb: Int,
    var innerList: MathList,
) : MathAtom(AtomType.COLOR, "")

/**
 * A delimiter at a fixed size, as in `\big(` or `\Bigr]`. [height] is in em.
 */
internal class SizedDelimiter(
    type: AtomType,
    val delimiter: String,
    val height: Float,
) : MathAtom(type, delimiter)

/**
 * A base with a script set above and/or below it, as in `\overset`, `\underset` and
 * `\stackrel`. Spaced like [type].
 */
internal class Stack(
    type: AtomType,
    var base: MathList,
    var over: MathList?,
    var under: MathList?,
) : MathAtom(type, "")

/**
 * `\boxed`: a frame drawn around [innerList].
 */
internal class Framed(
    var innerList: MathList,
) : MathAtom(AtomType.ORDINARY, "")

internal class MathTable(
    val cells: MutableList<MutableList<MathList>>,
    var environment: String? = null,
) : MathAtom(AtomType.TABLE, "") {
    private val alignments = mutableMapOf<Int, ColumnAlignment>()

    /**
     * Space before each column, in mu, keyed by column index. Missing columns use
     * [interColumnSpacing].
     */
    val columnGaps = mutableMapOf<Int, Float>()

    var interColumnSpacing: Float = 0f

    /**
     * Extra space between rows in jots (0.3 em).
     */
    var interRowAdditionalSpacing: Float = 0f

    val numColumns: Int
        get() = cells.maxOfOrNull { it.size } ?: 0

    val numRows: Int
        get() = cells.size

    fun setAlignment(column: Int, alignment: ColumnAlignment) {
        alignments[column] = alignment
    }

    fun alignment(column: Int): ColumnAlignment = alignments[column] ?: ColumnAlignment.CENTER

    fun gapBefore(column: Int): Float = columnGaps[column] ?: interColumnSpacing
}
