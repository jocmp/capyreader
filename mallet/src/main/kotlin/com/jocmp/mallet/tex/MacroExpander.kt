package com.jocmp.mallet.tex

/** A macro body: TeX source, pre-lexed tokens, or a function of the expander. */
sealed interface MacroDefinition

@JvmInline
value class MacroString(val text: String) : MacroDefinition

/** Tokens are in reverse order, ready to push onto the expander's stack. */
class MacroExpansion(
    val tokens: List<Token>,
    val numArgs: Int,
    val delimiters: List<List<String>>? = null,
    val unexpandable: Boolean = false,
) : MacroDefinition

/** Must return a [MacroString] or a [MacroExpansion]. */
class MacroFunction(val expand: (MacroContext) -> MacroDefinition) : MacroDefinition

class MacroArg(
    val tokens: MutableList<Token>,
    val start: Token,
    val end: Token,
)

val implicitCommands = setOf("^", "_", "\\limits", "\\nolimits")

/** KaTeX's "gullet", where macros are expanded until only non-macro tokens remain. */
class MacroExpander(
    input: String,
    val settings: Settings,
    override var mode: Mode,
) : MacroContext {
    var expansionCount = 0
    lateinit var lexer: Lexer
    override val macros: Namespace<MacroDefinition> = Namespace(Macros.builtins, settings.macros)

    /** Tokens in reverse order. */
    val stack = mutableListOf<Token>()

    init {
        feed(input)
    }

    fun feed(input: String) {
        lexer = Lexer(input, settings)
    }

    fun switchMode(newMode: Mode) {
        mode = newMode
    }

    fun beginGroup() = macros.beginGroup()

    fun endGroup() = macros.endGroup()

    fun endGroups() = macros.endGroups()

    override fun future(): Token {
        if (stack.isEmpty()) {
            pushToken(lexer.lex())
        }
        return stack.last()
    }

    override fun popToken(): Token {
        future()
        return stack.removeAt(stack.lastIndex)
    }

    fun pushToken(token: Token) {
        stack.add(token)
    }

    fun pushTokens(tokens: List<Token>) {
        stack.addAll(tokens)
    }

    /**
     * Finds a macro argument without expanding tokens and pushes its tokens onto the stack,
     * followed by an EOF marker. The returned token spans the argument.
     */
    fun scanArgument(isOptional: Boolean): Token? {
        val start: Token
        val end: Token
        val tokens: List<Token>
        if (isOptional) {
            consumeSpaces()
            if (future().text != "[") {
                return null
            }
            start = popToken()
            val arg = consumeArg(listOf("]"))
            tokens = arg.tokens
            end = arg.end
        } else {
            val arg = consumeArg()
            tokens = arg.tokens
            start = arg.start
            end = arg.end
        }
        pushToken(Token("EOF", end.loc))
        pushTokens(tokens)
        return Token("", SourceLocation.range(start, end))
    }

    override fun consumeSpaces() {
        while (future().text == " ") {
            stack.removeAt(stack.lastIndex)
        }
    }

    override fun consumeArg(delims: List<String>?): MacroArg {
        val tokens = mutableListOf<Token>()
        val isDelimited = !delims.isNullOrEmpty()
        if (!isDelimited) {
            consumeSpaces()
        }
        val start = future()
        var tok: Token
        var depth = 0
        var match = 0
        do {
            tok = popToken()
            tokens.add(tok)
            when (tok.text) {
                "{" -> depth += 1
                "}" -> {
                    depth -= 1
                    if (depth == -1) {
                        throw ParseError("Extra }", tok)
                    }
                }
                "EOF" -> {
                    val expected = if (delims != null && isDelimited) delims[match] else "}"
                    throw ParseError("Unexpected end of input in a macro argument, expected '$expected'", tok)
                }
            }
            if (delims != null && isDelimited) {
                if ((depth == 0 || (depth == 1 && delims[match] == "{")) && tok.text == delims[match]) {
                    match += 1
                    if (match == delims.size) {
                        repeat(match) { tokens.removeAt(tokens.lastIndex) }
                        break
                    }
                } else {
                    match = 0
                }
            }
        } while (depth != 0 || isDelimited)

        if (start.text == "{" && tokens.lastOrNull()?.text == "}") {
            tokens.removeAt(tokens.lastIndex)
            tokens.removeAt(0)
        }
        tokens.reverse()
        return MacroArg(tokens, start, tok)
    }

    override fun consumeArgs(numArgs: Int): List<List<Token>> = consumeArgs(numArgs, null)

    fun consumeArgs(numArgs: Int, delimiters: List<List<String>>?): List<List<Token>> {
        if (delimiters != null) {
            if (delimiters.size != numArgs + 1) {
                throw ParseError("The length of delimiters doesn't match the number of args!")
            }
            delimiters[0].forEach { delim ->
                val tok = popToken()
                if (delim != tok.text) {
                    throw ParseError("Use of the macro doesn't match its definition", tok)
                }
            }
        }
        return (0 until numArgs).map { i -> consumeArg(delimiters?.get(i + 1)).tokens }
    }

    private fun countExpansion(amount: Int) {
        expansionCount += amount
        if (expansionCount > settings.maxExpand) {
            throw ParseError("Too many expansions: infinite loop or need to increase maxExpand setting")
        }
    }

    /**
     * Expands the next token once if possible. Returns the number of tokens pushed, or null when
     * the token was not expandable and was left on the stack.
     */
    override fun expandOnce(expandableOnly: Boolean): Int? {
        val topToken = popToken()
        val name = topToken.text
        val expansion = if (!topToken.noexpand) getExpansion(name) else null
        if (expansion == null || (expandableOnly && expansion.unexpandable)) {
            if (expandableOnly && expansion == null && name.startsWith("\\") && !isDefined(name)) {
                throw ParseError("Undefined control sequence: $name")
            }
            pushToken(topToken)
            return null
        }
        countExpansion(1)
        var tokens = expansion.tokens
        val args = consumeArgs(expansion.numArgs, expansion.delimiters)
        if (expansion.numArgs > 0) {
            val copy = tokens.toMutableList()
            var i = copy.lastIndex
            while (i >= 0) {
                var tok = copy[i]
                if (tok.text == "#") {
                    if (i == 0) {
                        throw ParseError("Incomplete placeholder at end of macro body", tok)
                    }
                    i -= 1
                    tok = copy[i]
                    if (tok.text == "#") {
                        copy.removeAt(i + 1)
                    } else if (tok.text.length == 1 && tok.text[0] in '1'..'9') {
                        copy.removeAt(i)
                        copy.removeAt(i)
                        copy.addAll(i, args[tok.text.toInt() - 1])
                    } else {
                        throw ParseError("Not a valid argument number", tok)
                    }
                }
                i -= 1
            }
            tokens = copy
        }
        pushTokens(tokens)
        return tokens.size
    }

    override fun expandAfterFuture(): Token {
        expandOnce()
        return future()
    }

    override fun expandNextToken(): Token {
        while (true) {
            if (expandOnce() == null) {
                val token = stack.removeAt(stack.lastIndex)
                if (token.treatAsRelax) {
                    token.text = "\\relax"
                }
                return token
            }
        }
    }

    override fun expandMacro(name: String): List<Token>? = if (macros.has(name)) expandTokens(listOf(Token(name))) else null

    override fun expandTokens(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val oldStackLength = stack.size
        pushTokens(tokens)
        while (stack.size > oldStackLength) {
            if (expandOnce(true) == null) {
                val token = stack.removeAt(stack.lastIndex)
                if (token.treatAsRelax) {
                    token.noexpand = false
                    token.treatAsRelax = false
                }
                output.add(token)
            }
        }
        countExpansion(output.size)
        return output
    }

    override fun expandMacroAsText(name: String): String? = expandMacro(name)?.joinToString("") { it.text }

    private fun getExpansion(name: String): MacroExpansion? {
        val definition = macros.get(name) ?: return null
        if (name.length == 1) {
            val catcode = lexer.catcodes[name]
            if (catcode != null && catcode != 13) {
                return null
            }
        }
        val expansion = if (definition is MacroFunction) definition.expand(this) else definition
        return when (expansion) {
            is MacroString -> lexExpansion(expansion.text)
            is MacroExpansion -> expansion
            is MacroFunction -> throw IllegalStateException("Macro $name expanded to a function")
        }
    }

    private fun lexExpansion(expansion: String): MacroExpansion {
        var numArgs = 0
        if (expansion.contains("#")) {
            val stripped = expansion.replace("##", "")
            while (stripped.contains("#${numArgs + 1}")) {
                numArgs += 1
            }
        }
        val bodyLexer = Lexer(expansion, settings)
        val tokens = mutableListOf<Token>()
        var tok = bodyLexer.lex()
        while (tok.text != "EOF") {
            tokens.add(tok)
            tok = bodyLexer.lex()
        }
        tokens.reverse()
        return MacroExpansion(tokens, numArgs)
    }

    override fun isDefined(name: String): Boolean =
        macros.has(name) ||
            Functions.functions.containsKey(name) ||
            Symbols.math.containsKey(name) ||
            Symbols.text.containsKey(name) ||
            implicitCommands.contains(name)

    override fun isExpandable(name: String): Boolean {
        val macro = macros.get(name)
        if (macro != null) {
            return macro !is MacroExpansion || !macro.unexpandable
        }
        val function = Functions.functions[name]
        return function != null && !function.primitive
    }
}

/** What function-valued macros can see of the expander. */
interface MacroContext {
    val mode: Mode
    val macros: Namespace<MacroDefinition>

    fun future(): Token

    fun popToken(): Token

    fun consumeSpaces()

    fun expandOnce(expandableOnly: Boolean = false): Int?

    fun expandAfterFuture(): Token

    fun expandNextToken(): Token

    fun expandMacro(name: String): List<Token>?

    fun expandMacroAsText(name: String): String?

    fun expandTokens(tokens: List<Token>): List<Token>

    fun consumeArg(delims: List<String>? = null): MacroArg

    fun consumeArgs(numArgs: Int): List<List<Token>>

    fun isDefined(name: String): Boolean

    fun isExpandable(name: String): Boolean
}
