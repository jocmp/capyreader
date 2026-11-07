package com.jocmp.mallet.math

/**
 * [MathFont] scaled to [fontSize]. All values are in the same unit as [fontSize].
 */
internal class MathMetrics(
    val font: MathFont,
    val fontSize: Float,
) {
    private val scale = fontSize / font.unitsPerEm

    fun withSize(size: Float): MathMetrics = MathMetrics(font, size)

    val muUnit: Float
        get() = fontSize / 18f

    val axisHeight: Float get() = value(MathConstant.AxisHeight)
    val scriptScaleDown: Float get() = percent(MathConstant.ScriptPercentScaleDown)
    val scriptScriptScaleDown: Float get() = percent(MathConstant.ScriptScriptPercentScaleDown)

    val fractionNumeratorDisplayStyleShiftUp: Float get() = value(MathConstant.FractionNumeratorDisplayStyleShiftUp)
    val fractionNumeratorShiftUp: Float get() = value(MathConstant.FractionNumeratorShiftUp)
    val fractionDenominatorDisplayStyleShiftDown: Float get() = value(MathConstant.FractionDenominatorDisplayStyleShiftDown)
    val fractionDenominatorShiftDown: Float get() = value(MathConstant.FractionDenominatorShiftDown)
    val fractionNumeratorDisplayStyleGapMin: Float get() = value(MathConstant.FractionNumDisplayStyleGapMin)
    val fractionNumeratorGapMin: Float get() = value(MathConstant.FractionNumeratorGapMin)
    val fractionDenominatorDisplayStyleGapMin: Float get() = value(MathConstant.FractionDenomDisplayStyleGapMin)
    val fractionDenominatorGapMin: Float get() = value(MathConstant.FractionDenominatorGapMin)
    val fractionRuleThickness: Float get() = value(MathConstant.FractionRuleThickness)

    // Not OpenType constants. LuaTeX's and KaTeX's \sigma_20 and \sigma_21 equivalents.
    val fractionDelimiterSize: Float get() = 1.01f * fontSize
    val fractionDelimiterDisplayStyleSize: Float get() = 2.39f * fontSize

    val superscriptShiftUp: Float get() = value(MathConstant.SuperscriptShiftUp)
    val superscriptShiftUpCramped: Float get() = value(MathConstant.SuperscriptShiftUpCramped)
    val subscriptShiftDown: Float get() = value(MathConstant.SubscriptShiftDown)
    val superscriptBaselineDropMax: Float get() = value(MathConstant.SuperscriptBaselineDropMax)
    val subscriptBaselineDropMin: Float get() = value(MathConstant.SubscriptBaselineDropMin)
    val superscriptBottomMin: Float get() = value(MathConstant.SuperscriptBottomMin)
    val subscriptTopMax: Float get() = value(MathConstant.SubscriptTopMax)
    val subSuperscriptGapMin: Float get() = value(MathConstant.SubSuperscriptGapMin)
    val superscriptBottomMaxWithSubscript: Float get() = value(MathConstant.SuperscriptBottomMaxWithSubscript)
    val spaceAfterScript: Float get() = value(MathConstant.SpaceAfterScript)

    val radicalRuleThickness: Float get() = value(MathConstant.RadicalRuleThickness)
    val radicalExtraAscender: Float get() = value(MathConstant.RadicalExtraAscender)
    val radicalVerticalGap: Float get() = value(MathConstant.RadicalVerticalGap)
    val radicalDisplayStyleVerticalGap: Float get() = value(MathConstant.RadicalDisplayStyleVerticalGap)
    val radicalKernBeforeDegree: Float get() = value(MathConstant.RadicalKernBeforeDegree)
    val radicalKernAfterDegree: Float get() = value(MathConstant.RadicalKernAfterDegree)
    val radicalDegreeBottomRaisePercent: Float get() = percent(MathConstant.RadicalDegreeBottomRaisePercent)

    val upperLimitGapMin: Float get() = value(MathConstant.UpperLimitGapMin)
    val upperLimitBaselineRiseMin: Float get() = value(MathConstant.UpperLimitBaselineRiseMin)
    val lowerLimitGapMin: Float get() = value(MathConstant.LowerLimitGapMin)
    val lowerLimitBaselineDropMin: Float get() = value(MathConstant.LowerLimitBaselineDropMin)

    val accentBaseHeight: Float get() = value(MathConstant.AccentBaseHeight)

    val overbarExtraAscender: Float get() = value(MathConstant.OverbarExtraAscender)
    val overbarRuleThickness: Float get() = value(MathConstant.OverbarRuleThickness)
    val overbarVerticalGap: Float get() = value(MathConstant.OverbarVerticalGap)
    val underbarExtraDescender: Float get() = value(MathConstant.UnderbarExtraDescender)
    val underbarRuleThickness: Float get() = value(MathConstant.UnderbarRuleThickness)
    val underbarVerticalGap: Float get() = value(MathConstant.UnderbarVerticalGap)

    val stackBottomDisplayStyleShiftDown: Float get() = value(MathConstant.StackBottomDisplayStyleShiftDown)
    val stackBottomShiftDown: Float get() = value(MathConstant.StackBottomShiftDown)
    val stackDisplayStyleGapMin: Float get() = value(MathConstant.StackDisplayStyleGapMin)
    val stackGapMin: Float get() = value(MathConstant.StackGapMin)
    val stackTopDisplayStyleShiftUp: Float get() = value(MathConstant.StackTopDisplayStyleShiftUp)
    val stackTopShiftUp: Float get() = value(MathConstant.StackTopShiftUp)

    val minConnectorOverlap: Float get() = units(font.minConnectorOverlap)

    fun glyph(codepoint: Int): Int = font.glyphForCodepoint(codepoint)

    fun advance(glyph: Int): Float = units(font.advance(glyph))

    fun ascent(glyph: Int): Float {
        val bounds = font.bounds(glyph)
        return maxOf(0f, units(maxOf(bounds.yMax, bounds.yMin)))
    }

    fun descent(glyph: Int): Float {
        val bounds = font.bounds(glyph)
        return maxOf(0f, -units(minOf(bounds.yMax, bounds.yMin)))
    }

    fun inkWidth(glyph: Int): Float {
        val bounds = font.bounds(glyph)
        return units(maxOf(bounds.xMin, bounds.xMax))
    }

    fun italicCorrection(glyph: Int): Float = units(font.italicCorrection(glyph))

    fun topAccentAdjustment(glyph: Int): Float {
        val attachment = font.topAccentAttachment(glyph) ?: return advance(glyph) / 2
        return units(attachment)
    }

    fun verticalVariants(glyph: Int): IntArray = font.verticalVariants(glyph)

    fun horizontalVariants(glyph: Int): IntArray = font.horizontalVariants(glyph)

    fun verticalAssembly(glyph: Int): List<ScaledGlyphPart>? =
        font.verticalAssembly(glyph)?.map { part ->
            ScaledGlyphPart(
                glyph = part.glyph,
                startConnectorLength = units(part.startConnectorLength),
                endConnectorLength = units(part.endConnectorLength),
                fullAdvance = units(part.fullAdvance),
                isExtender = part.isExtender,
            )
        }

    /**
     * The first vertical variant that differs from [glyph], used to enlarge operators in display
     * style.
     */
    fun largerGlyph(glyph: Int): Int = verticalVariants(glyph).firstOrNull { it != glyph } ?: glyph

    private fun value(constant: MathConstant): Float = units(font.constant(constant))

    private fun percent(constant: MathConstant): Float = font.constant(constant) / 100f

    private fun units(value: Int): Float = value * scale
}

internal data class ScaledGlyphPart(
    val glyph: Int,
    val startConnectorLength: Float,
    val endConnectorLength: Float,
    val fullAdvance: Float,
    val isExtender: Boolean,
)
