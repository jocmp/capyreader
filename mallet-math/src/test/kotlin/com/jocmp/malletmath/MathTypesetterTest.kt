package com.jocmp.malletmath

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Expected dimensions come from AndroidMath's TypesetterInstrumentedTest, which typeset the same
 * formulas in Latin Modern Math at 20pt through FreeType.
 */
class MathTypesetterTest {
    @Test
    fun simpleVariable() = assertDimensions("x", ascent = 8.834f, descent = 0.24f, width = 11.44f)

    @Test
    fun multipleVariables() = assertDimensions("xyzw", ascent = 8.834f, descent = 4.12f, width = 44.86f)

    @Test
    fun variablesAndNumbers() = assertDimensions("xy2w", ascent = 13.32f, descent = 4.12f, width = 45.56f)

    @Test
    fun equationWithOperatorsAndRelations() =
        assertDimensions("2x+3=y", ascent = 13.32f, descent = 4.12f, width = 92.36f)

    @Test
    fun superscript() = assertDimensions("x^2", ascent = 16.584f, descent = 0.24f, width = 18.44f)

    @Test
    fun subscript() = assertDimensions("x_1", ascent = 8.834f, descent = 4.954f, width = 18.44f)

    @Test
    fun superSubscript() = assertDimensions("x^2_1", ascent = 16.584f, descent = 5.292f, width = 18.44f)

    @Test
    fun radical() = assertDimensions("\\sqrt{1}", ascent = 19.34f, descent = 1.48f, width = 26.66f)

    @Test
    fun radicalWithDegree() = assertDimensions("\\sqrt[3]{1}", ascent = 19.34f, descent = 1.48f, width = 26.66f)

    @Test
    fun fraction() = assertDimensions("\\frac{1}{3}", ascent = 26.86f, descent = 14.18f, width = 10f)

    @Test
    fun atop() = assertDimensions("1 \\atop 3", ascent = 26.86f, descent = 14.18f, width = 10f)

    @Test
    fun binomial() = assertDimensions("\\binom{1}{3}", ascent = 28.93f, descent = 18.93f, width = 39.44f)

    @Test
    fun largeOperatorText() = assertDimensions("\\sin x", ascent = 13.14f, descent = 0.24f, width = 39.33f)

    @Test
    fun largeOperatorSymbol() = assertDimensions("\\int x", ascent = 27.23f, descent = 17.23f, width = 34.753f)

    @Test
    fun largeOperatorSymbolWithScripts() =
        assertDimensions("\\int^1_0 x", ascent = 33.054f, descent = 20.352f, width = 42.873f)

    @Test
    fun largeOperatorTextWithLimits() =
        assertDimensions("\\lim_\\infty x", ascent = 13.88f, descent = 12.188f, width = 42.553f)

    @Test
    fun largeOperatorSymbolWithLimits() =
        assertDimensions("\\sum^\\infty_0 x", ascent = 29.366f, descent = 21.996f, width = 43.653f)

    @Test
    fun inner() = assertDimensions("\\left( x \\right)", ascent = 14.97f, descent = 4.97f, width = 27f)

    @Test
    fun overline() = assertDimensions("\\overline 1", ascent = 17.32f, descent = 0.02f, width = 10f)

    @Test
    fun underline() = assertDimensions("\\underline 1", ascent = 13.32f, descent = 4.02f, width = 10f)

    @Test
    fun largeRadicalDescent() =
        assertDimensions(
            "\\sqrt{\\frac{\\sqrt{\\frac{1}{2}} + 3}{\\sqrt{5}^x}}",
            ascent = 49.18f,
            descent = 21.308f,
            width = 82.569f,
        )

    @Test
    fun accent() = assertDimensions("\\hat x", ascent = 14.68f, descent = 0.24f, width = 11.44f)

    @Test
    fun wideAccent() = assertDimensions("\\hat{xyzw}", ascent = 14.98f, descent = 4.12f, width = 44.86f)

    @Test
    fun layingOutTwiceGivesTheSameBox() {
        val list = MathParser("12 + ab").parse()
        val metrics = MathMetrics(MathFont.default, FONT_SIZE)

        val first = MathTypesetter.layout(list, metrics, LineStyle.DISPLAY)
        val second = MathTypesetter.layout(list, metrics, LineStyle.DISPLAY)

        assertEquals(first.width, second.width)
    }

    @Test
    fun leadingMinusAfterStyleChangeIsUnary() {
        val plain = layout("-x", display = false)
        val styled = layout("\\textstyle -x", display = false)

        assertEquals(plain.width, styled.width, TOLERANCE)
    }

    @Test
    fun thinSpacesSeparateDigitGroups() {
        val grouped = layout("10\\,000", display = false)
        val plain = layout("10000", display = false)

        assert(grouped.width > plain.width)
    }

    @Test
    fun textStyleFractionIsSmallerThanDisplay() {
        val display = layout("\\frac{1}{2}", display = true)
        val inline = layout("\\frac{1}{2}", display = false)

        assert(inline.height < display.height)
    }

    private fun assertDimensions(
        tex: String,
        ascent: Float,
        descent: Float,
        width: Float,
    ) {
        val box = layout(tex, display = true)

        assertEquals(ascent, box.ascent, TOLERANCE, "ascent of $tex")
        assertEquals(descent, box.descent, TOLERANCE, "descent of $tex")
        assertEquals(width, box.width, TOLERANCE, "width of $tex")
    }

    private fun layout(tex: String, display: Boolean): MathBox =
        MalletMath.layout(tex, fontSize = FONT_SIZE, display = display).getOrThrow()

    private companion object {
        const val FONT_SIZE = 20f
        const val TOLERANCE = 0.05f
    }
}
