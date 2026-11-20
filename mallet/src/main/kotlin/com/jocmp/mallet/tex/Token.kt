package com.jocmp.mallet.tex

interface LexerInterface {
    val input: String
}

class SourceLocation(
    val lexer: LexerInterface,
    val start: Int,
    val end: Int,
) {
    companion object {
        fun range(first: Located?, second: Located? = null): SourceLocation? {
            if (second == null) {
                return first?.loc
            }
            val firstLoc = first?.loc ?: return null
            val secondLoc = second.loc ?: return null
            if (firstLoc.lexer !== secondLoc.lexer) {
                return null
            }
            return SourceLocation(firstLoc.lexer, firstLoc.start, secondLoc.end)
        }
    }
}

interface Located {
    val loc: SourceLocation?
}

class Token(
    var text: String,
    override val loc: SourceLocation? = null,
) : Located {
    var noexpand: Boolean = false
    var treatAsRelax: Boolean = false

    fun range(endToken: Token, text: String): Token = Token(text, SourceLocation.range(this, endToken))
}
