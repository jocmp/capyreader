package com.jocmp.mallet.math

/**
 * Typesets KaTeX-flavored TeX into a [MathBox] that a renderer can draw.
 */
object MalletMath {
    /**
     * @param tex TeX source without delimiters such as `$` or `\[`.
     * @param fontSize em size of the formula, in the unit the caller draws in.
     * @param display true for display (block) math, false for inline math.
     * @return the typeset box, or a failure for TeX outside the supported subset.
     */
    fun layout(
        tex: String,
        fontSize: Float,
        display: Boolean,
        font: MathFont = MathFont.default,
    ): Result<MathBox> =
        runCatching {
            val list = MathParser(tex).parse()
            val style = if (display) LineStyle.DISPLAY else LineStyle.TEXT
            MathTypesetter.layout(list, MathMetrics(font, fontSize), style)
        }
}
