package com.jocmp.malletmath

/**
 * Maps characters into the Unicode Mathematical Alphanumeric Symbols block for a [FontStyle].
 */
internal object MathCharset {
    fun styled(text: String, style: FontStyle): String {
        val builder = StringBuilder()
        text.codePoints().forEach { codepoint ->
            builder.appendCodePoint(styled(codepoint, style))
        }
        return builder.toString()
    }

    private fun styled(ch: Int, style: FontStyle): Int =
        when (style) {
            FontStyle.DEFAULT -> defaultStyle(ch)
            FontStyle.ROMAN -> ch
            FontStyle.BOLD -> bold(ch)
            FontStyle.ITALIC -> italic(ch)
            FontStyle.BOLD_ITALIC -> boldItalic(ch)
            FontStyle.CALIGRAPHIC -> caligraphic(ch)
            FontStyle.TYPEWRITER -> typewriter(ch)
            FontStyle.SANS_SERIF -> sansSerif(ch)
            FontStyle.FRAKTUR -> fraktur(ch)
            FontStyle.BLACKBOARD -> blackboard(ch)
        }

    /**
     * LaTeX's default: Latin and lowercase Greek letters are italic, numbers and capital Greek
     * are upright.
     */
    private fun defaultStyle(ch: Int): Int =
        when {
            isLatin(ch) || isLowerGreek(ch) || greekSymbolOrder(ch) >= 0 -> italic(ch)
            else -> ch
        }

    private fun italic(ch: Int): Int =
        when {
            // Planck's constant stands in for the missing italic h.
            ch == 'h'.code -> 0x210E
            isUpper(ch) -> 0x1D434 + (ch - 'A'.code)
            isLower(ch) -> 0x1D44E + (ch - 'a'.code)
            isCapitalGreek(ch) -> 0x1D6E2 + (ch - GREEK_CAPITAL_START)
            isLowerGreek(ch) -> 0x1D6FC + (ch - GREEK_LOWER_START)
            greekSymbolOrder(ch) >= 0 -> 0x1D716 + greekSymbolOrder(ch)
            else -> ch
        }

    private fun bold(ch: Int): Int =
        when {
            isUpper(ch) -> 0x1D400 + (ch - 'A'.code)
            isLower(ch) -> 0x1D41A + (ch - 'a'.code)
            isCapitalGreek(ch) -> 0x1D6A8 + (ch - GREEK_CAPITAL_START)
            isLowerGreek(ch) -> 0x1D6C2 + (ch - GREEK_LOWER_START)
            greekSymbolOrder(ch) >= 0 -> 0x1D6DC + greekSymbolOrder(ch)
            isDigit(ch) -> 0x1D7CE + (ch - '0'.code)
            else -> ch
        }

    private fun boldItalic(ch: Int): Int =
        when {
            isUpper(ch) -> 0x1D468 + (ch - 'A'.code)
            isLower(ch) -> 0x1D482 + (ch - 'a'.code)
            isCapitalGreek(ch) -> 0x1D71C + (ch - GREEK_CAPITAL_START)
            isLowerGreek(ch) -> 0x1D736 + (ch - GREEK_LOWER_START)
            greekSymbolOrder(ch) >= 0 -> 0x1D750 + greekSymbolOrder(ch)
            // There are no bold italic digits.
            isDigit(ch) -> bold(ch)
            else -> ch
        }

    private fun caligraphic(ch: Int): Int {
        caligraphicExceptions[ch]?.let { return it }
        return when {
            isUpper(ch) -> 0x1D49C + (ch - 'A'.code)
            // Latin Modern Math has no lowercase script letters.
            else -> defaultStyle(ch)
        }
    }

    private fun typewriter(ch: Int): Int =
        when {
            isUpper(ch) -> 0x1D670 + (ch - 'A'.code)
            isLower(ch) -> 0x1D68A + (ch - 'a'.code)
            isDigit(ch) -> 0x1D7F6 + (ch - '0'.code)
            else -> defaultStyle(ch)
        }

    private fun sansSerif(ch: Int): Int =
        when {
            isUpper(ch) -> 0x1D5A0 + (ch - 'A'.code)
            isLower(ch) -> 0x1D5BA + (ch - 'a'.code)
            isDigit(ch) -> 0x1D7E2 + (ch - '0'.code)
            else -> defaultStyle(ch)
        }

    private fun fraktur(ch: Int): Int {
        frakturExceptions[ch]?.let { return it }
        return when {
            isUpper(ch) -> 0x1D504 + (ch - 'A'.code)
            isLower(ch) -> 0x1D51E + (ch - 'a'.code)
            else -> defaultStyle(ch)
        }
    }

    private fun blackboard(ch: Int): Int {
        blackboardExceptions[ch]?.let { return it }
        return when {
            isUpper(ch) -> 0x1D538 + (ch - 'A'.code)
            isLower(ch) -> 0x1D552 + (ch - 'a'.code)
            isDigit(ch) -> 0x1D7D8 + (ch - '0'.code)
            else -> defaultStyle(ch)
        }
    }

    private fun isUpper(ch: Int) = ch in 'A'.code..'Z'.code

    private fun isLower(ch: Int) = ch in 'a'.code..'z'.code

    private fun isLatin(ch: Int) = isUpper(ch) || isLower(ch)

    private fun isDigit(ch: Int) = ch in '0'.code..'9'.code

    private fun isLowerGreek(ch: Int) = ch in GREEK_LOWER_START..GREEK_LOWER_END

    private fun isCapitalGreek(ch: Int) = ch in GREEK_CAPITAL_START..GREEK_CAPITAL_END

    // Epsilon, vartheta, varkappa, phi, varrho and varpi follow the alphabet in this order.
    private fun greekSymbolOrder(ch: Int): Int = greekSymbols.indexOf(ch)

    private const val GREEK_LOWER_START = 0x03B1
    private const val GREEK_LOWER_END = 0x03C9
    private const val GREEK_CAPITAL_START = 0x0391
    private const val GREEK_CAPITAL_END = 0x03A9

    private val greekSymbols = listOf(0x03F5, 0x03D1, 0x03F0, 0x03D5, 0x03F1, 0x03D6)

    private val caligraphicExceptions =
        mapOf(
            'B'.code to 0x212C,
            'E'.code to 0x2130,
            'F'.code to 0x2131,
            'H'.code to 0x210B,
            'I'.code to 0x2110,
            'L'.code to 0x2112,
            'M'.code to 0x2133,
            'R'.code to 0x211B,
            'e'.code to 0x212F,
            'g'.code to 0x210A,
            'o'.code to 0x2134,
        )

    private val frakturExceptions =
        mapOf(
            'C'.code to 0x212D,
            'H'.code to 0x210C,
            'I'.code to 0x2111,
            'R'.code to 0x211C,
            'Z'.code to 0x2128,
        )

    private val blackboardExceptions =
        mapOf(
            'C'.code to 0x2102,
            'H'.code to 0x210D,
            'N'.code to 0x2115,
            'P'.code to 0x2119,
            'Q'.code to 0x211A,
            'R'.code to 0x211D,
            'Z'.code to 0x2124,
        )
}
