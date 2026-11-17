package com.jocmp.mallet.tex

private const val SPACE = "[ \r\n\t]"
private const val CONTROL_WORD = "\\\\[a-zA-Z@]+"
private const val CONTROL_SYMBOL = "\\\\[^\uD800-\uDFFF]"
private const val CONTROL_WORD_WHITESPACE = "($CONTROL_WORD)$SPACE*"
private const val CONTROL_SPACE = "\\\\(\n|[ \r\t]+\n?)[ \r\t]*"
private const val COMBINING_DIACRITICAL_MARK = "[̀-ͯ]"

val combiningDiacriticalMarksEndRegex = Regex("$COMBINING_DIACRITICAL_MARK+$")

// Java regexes match a surrogate pair as one code point, so KaTeX's surrogate-pair branch
// becomes a supplementary code point range.
private val defaultCatcodes: Map<String, Int> = mapOf("%" to 14, "~" to 13)

private val tokenRegex =
    Regex(
        "($SPACE+)|" +
            "$CONTROL_SPACE|" +
            "([!-\\[\\]-‧‪-퟿豈-￿]" +
            "$COMBINING_DIACRITICAL_MARK*" +
            "|[\\x{10000}-\\x{10FFFF}]" +
            "$COMBINING_DIACRITICAL_MARK*" +
            "|\\\\verb\\*([\\s\\S]).*?\\4" +
            "|\\\\verb([^*a-zA-Z]).*?\\5" +
            "|$CONTROL_WORD_WHITESPACE" +
            "|$CONTROL_SYMBOL)",
    )

class Lexer(
    override val input: String,
    val settings: Settings,
) : LexerInterface {
    private var catcodeOverrides: MutableMap<String, Int>? = null

    /** KaTeX's per-lexer catcode table, shared until a lexer changes it: one is made per macro expansion. */
    val catcodes: Map<String, Int>
        get() = catcodeOverrides ?: defaultCatcodes
    private val matcher = tokenRegex.toPattern().matcher(input)
    private var lastIndex = 0

    fun setCatcode(char: String, code: Int?) {
        val table = catcodeOverrides ?: HashMap(defaultCatcodes).also { catcodeOverrides = it }
        if (code == null) {
            table.remove(char)
        } else {
            table[char] = code
        }
    }

    fun lex(): Token {
        val pos = lastIndex
        if (pos == input.length) {
            return Token("EOF", SourceLocation(this, pos, pos))
        }
        matcher.region(pos, input.length)
        if (!matcher.lookingAt()) {
            val end = input.offsetByCodePoints(pos, 1)
            val character = input.substring(pos, end)
            throw ParseError("Unexpected character: '$character'", Token(character, SourceLocation(this, pos, end)))
        }
        lastIndex = matcher.end()
        val text = matcher.group(6) ?: matcher.group(3) ?: if (matcher.group(2) != null) "\\ " else " "

        if (catcodes[text] == 14) {
            val nlIndex = input.indexOf('\n', lastIndex)
            lastIndex = if (nlIndex == -1) input.length else nlIndex + 1
            return lex()
        }
        return Token(text, SourceLocation(this, pos, lastIndex))
    }
}
