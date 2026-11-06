package com.jocmp.malletmath

import java.io.DataInputStream
import java.io.InputStream

/**
 * Glyph metrics, outlines and OpenType MATH data for one math font, in font units.
 *
 * Loaded from the binary written by `scripts/extract_font.py`. Use [MathFont.default]
 * rather than loading it again; the first access reads about 700 KB, so do it off the
 * main thread.
 */
class MathFont internal constructor(
    val unitsPerEm: Int,
    private val constants: IntArray,
    internal val minConnectorOverlap: Int,
    private val cmap: Map<Int, Int>,
    private val glyphs: Map<Int, GlyphData>,
    private val verticalVariants: Map<Int, IntArray>,
    private val horizontalVariants: Map<Int, IntArray>,
    private val verticalAssemblies: Map<Int, List<GlyphPart>>,
) {
    /**
     * Calls [visitor] with each outline command for [glyph], in font units with y pointing up.
     */
    fun visitOutline(glyph: Int, visitor: OutlineVisitor) {
        val data = glyphs[glyph] ?: return
        val coords = data.coords
        var c = 0

        data.ops.forEach { op ->
            when (op.toInt()) {
                OP_MOVE -> {
                    visitor.moveTo(coords[c].toFloat(), coords[c + 1].toFloat())
                    c += 2
                }

                OP_LINE -> {
                    visitor.lineTo(coords[c].toFloat(), coords[c + 1].toFloat())
                    c += 2
                }

                OP_QUAD -> {
                    visitor.quadTo(
                        coords[c].toFloat(),
                        coords[c + 1].toFloat(),
                        coords[c + 2].toFloat(),
                        coords[c + 3].toFloat(),
                    )
                    c += 4
                }

                OP_CUBIC -> {
                    visitor.cubicTo(
                        coords[c].toFloat(),
                        coords[c + 1].toFloat(),
                        coords[c + 2].toFloat(),
                        coords[c + 3].toFloat(),
                        coords[c + 4].toFloat(),
                        coords[c + 5].toFloat(),
                    )
                    c += 6
                }

                OP_CLOSE -> visitor.close()
            }
        }
    }

    internal fun constant(constant: MathConstant): Int = constants[constant.ordinal]

    internal fun glyphForCodepoint(codepoint: Int): Int = cmap[codepoint] ?: 0

    internal fun advance(glyph: Int): Int = glyphs[glyph]?.advance ?: 0

    internal fun bounds(glyph: Int): GlyphBounds = glyphs[glyph]?.bounds ?: GlyphBounds.EMPTY

    internal fun italicCorrection(glyph: Int): Int = glyphs[glyph]?.italicCorrection ?: 0

    internal fun topAccentAttachment(glyph: Int): Int? = glyphs[glyph]?.topAccent

    internal fun verticalVariants(glyph: Int): IntArray = verticalVariants[glyph] ?: intArrayOf(glyph)

    internal fun horizontalVariants(glyph: Int): IntArray = horizontalVariants[glyph] ?: intArrayOf(glyph)

    internal fun verticalAssembly(glyph: Int): List<GlyphPart>? = verticalAssemblies[glyph]

    interface OutlineVisitor {
        fun moveTo(x: Float, y: Float)

        fun lineTo(x: Float, y: Float)

        fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float)

        fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

        fun close()
    }

    companion object {
        private const val RESOURCE = "/com/jocmp/malletmath/math-glyphs.bin"
        private const val NO_ACCENT = 0x7FFF
        private const val OP_MOVE = 0
        private const val OP_LINE = 1
        private const val OP_QUAD = 2
        private const val OP_CUBIC = 3
        private const val OP_CLOSE = 4

        private val defaultFont =
            lazy {
                val stream =
                    MathFont::class.java.getResourceAsStream(RESOURCE)
                        ?: error("Missing math font resource $RESOURCE")

                stream.use { read(it) }
            }

        /**
         * Latin Modern Math, the Computer Modern lineage KaTeX's own fonts come from.
         */
        val default: MathFont
            get() = defaultFont.value

        /**
         * True once [default] has loaded, after which layout is cheap enough for the main thread.
         */
        val isDefaultLoaded: Boolean
            get() = defaultFont.isInitialized()

        internal fun read(input: InputStream): MathFont {
            val data = DataInputStream(input.buffered())
            val magic = ByteArray(4).also { data.readFully(it) }
            require(magic.decodeToString() == "MMF1") { "Not a mallet-math font file" }

            val unitsPerEm = data.readUnsignedShort()
            val constants = IntArray(data.readUnsignedShort()) { data.readShort().toInt() }
            val minConnectorOverlap = data.readUnsignedShort()

            val cmap = HashMap<Int, Int>()
            repeat(data.readInt()) {
                cmap[data.readInt()] = data.readUnsignedShort()
            }

            val glyphs = HashMap<Int, GlyphData>()
            repeat(data.readUnsignedShort()) {
                val gid = data.readUnsignedShort()
                glyphs[gid] = readGlyph(data)
            }

            val verticalVariants = HashMap<Int, IntArray>()
            val verticalAssemblies = HashMap<Int, List<GlyphPart>>()
            repeat(data.readUnsignedShort()) {
                val gid = data.readUnsignedShort()
                verticalVariants[gid] = IntArray(data.readUnsignedShort()) { data.readUnsignedShort() }
                val parts = List(data.readUnsignedShort()) {
                    GlyphPart(
                        glyph = data.readUnsignedShort(),
                        startConnectorLength = data.readShort().toInt(),
                        endConnectorLength = data.readShort().toInt(),
                        fullAdvance = data.readShort().toInt(),
                        isExtender = data.readUnsignedShort() and 1 == 1,
                    )
                }
                if (parts.isNotEmpty()) {
                    verticalAssemblies[gid] = parts
                }
            }

            val horizontalVariants = HashMap<Int, IntArray>()
            repeat(data.readUnsignedShort()) {
                val gid = data.readUnsignedShort()
                horizontalVariants[gid] = IntArray(data.readUnsignedShort()) { data.readUnsignedShort() }
            }

            return MathFont(
                unitsPerEm = unitsPerEm,
                constants = constants,
                minConnectorOverlap = minConnectorOverlap,
                cmap = cmap,
                glyphs = glyphs,
                verticalVariants = verticalVariants,
                horizontalVariants = horizontalVariants,
                verticalAssemblies = verticalAssemblies,
            )
        }

        private fun readGlyph(data: DataInputStream): GlyphData {
            val advance = data.readShort().toInt()
            val bounds =
                GlyphBounds(
                    xMin = data.readShort().toInt(),
                    yMin = data.readShort().toInt(),
                    xMax = data.readShort().toInt(),
                    yMax = data.readShort().toInt(),
                )
            val italicCorrection = data.readShort().toInt()
            val topAccent = data.readShort().toInt().takeIf { it != NO_ACCENT }

            val opCount = data.readUnsignedShort()
            val ops = ByteArray(opCount)
            val coords = ArrayList<Short>()
            repeat(opCount) { i ->
                val op = data.readUnsignedByte()
                ops[i] = op.toByte()
                repeat(coordCount(op)) { coords.add(data.readShort()) }
            }

            return GlyphData(
                advance = advance,
                bounds = bounds,
                italicCorrection = italicCorrection,
                topAccent = topAccent,
                ops = ops,
                coords = coords.toShortArray(),
            )
        }

        private fun coordCount(op: Int): Int =
            when (op) {
                OP_MOVE, OP_LINE -> 2
                OP_QUAD -> 4
                OP_CUBIC -> 6
                else -> 0
            }
    }
}

internal class GlyphData(
    val advance: Int,
    val bounds: GlyphBounds,
    val italicCorrection: Int,
    val topAccent: Int?,
    val ops: ByteArray,
    val coords: ShortArray,
)

internal data class GlyphBounds(
    val xMin: Int,
    val yMin: Int,
    val xMax: Int,
    val yMax: Int,
) {
    companion object {
        val EMPTY = GlyphBounds(0, 0, 0, 0)
    }
}

internal data class GlyphPart(
    val glyph: Int,
    val startConnectorLength: Int,
    val endConnectorLength: Int,
    val fullAdvance: Int,
    val isExtender: Boolean,
)

/**
 * OpenType MATH constants, in the order `extract_font.py` writes them.
 */
internal enum class MathConstant {
    ScriptPercentScaleDown,
    ScriptScriptPercentScaleDown,
    DelimitedSubFormulaMinHeight,
    DisplayOperatorMinHeight,
    MathLeading,
    AxisHeight,
    AccentBaseHeight,
    FlattenedAccentBaseHeight,
    SubscriptShiftDown,
    SubscriptTopMax,
    SubscriptBaselineDropMin,
    SuperscriptShiftUp,
    SuperscriptShiftUpCramped,
    SuperscriptBottomMin,
    SuperscriptBaselineDropMax,
    SubSuperscriptGapMin,
    SuperscriptBottomMaxWithSubscript,
    SpaceAfterScript,
    UpperLimitGapMin,
    UpperLimitBaselineRiseMin,
    LowerLimitGapMin,
    LowerLimitBaselineDropMin,
    StackTopShiftUp,
    StackTopDisplayStyleShiftUp,
    StackBottomShiftDown,
    StackBottomDisplayStyleShiftDown,
    StackGapMin,
    StackDisplayStyleGapMin,
    StretchStackTopShiftUp,
    StretchStackBottomShiftDown,
    StretchStackGapAboveMin,
    StretchStackGapBelowMin,
    FractionNumeratorShiftUp,
    FractionNumeratorDisplayStyleShiftUp,
    FractionDenominatorShiftDown,
    FractionDenominatorDisplayStyleShiftDown,
    FractionNumeratorGapMin,
    FractionNumDisplayStyleGapMin,
    FractionRuleThickness,
    FractionDenominatorGapMin,
    FractionDenomDisplayStyleGapMin,
    SkewedFractionHorizontalGap,
    SkewedFractionVerticalGap,
    OverbarVerticalGap,
    OverbarRuleThickness,
    OverbarExtraAscender,
    UnderbarVerticalGap,
    UnderbarRuleThickness,
    UnderbarExtraDescender,
    RadicalVerticalGap,
    RadicalDisplayStyleVerticalGap,
    RadicalRuleThickness,
    RadicalExtraAscender,
    RadicalKernBeforeDegree,
    RadicalKernAfterDegree,
    RadicalDegreeBottomRaisePercent,
}
