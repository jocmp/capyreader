package com.jocmp.mallet.tex

class ParseError(
    val rawMessage: String,
    token: Located? = null,
) : Exception(format(rawMessage, token?.loc)) {
    val position: Int? = token?.loc?.takeIf { it.start <= it.end }?.start
    val length: Int? = token?.loc?.takeIf { it.start <= it.end }?.let { it.end - it.start }

    private companion object {
        fun format(message: String, loc: SourceLocation?): String {
            val error = "KaTeX parse error: $message"
            if (loc == null || loc.start > loc.end) {
                return error
            }
            val input = loc.lexer.input
            val start = loc.start
            val end = loc.end
            val position = if (start == input.length) " at end of input: " else " at position ${start + 1}: "
            val underlined = input.substring(start, end).map { "$it̲" }.joinToString("")
            val left = if (start > 15) "…" + input.substring(start - 15, start) else input.substring(0, start)
            val right = if (end + 15 < input.length) input.substring(end, end + 15) + "…" else input.substring(end)
            return error + position + left + underlined + right
        }
    }
}
