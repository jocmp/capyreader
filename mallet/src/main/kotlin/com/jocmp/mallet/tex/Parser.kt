package com.jocmp.mallet.tex

private val endOfExpression = setOf("}", "\\endgroup", "\\end", "\\right", "&")
private val colorRegex = Regex("^(#[a-f0-9]{3,4}|#[a-f0-9]{6}|#[a-f0-9]{8}|[a-f0-9]{6}|[a-z]+)$", RegexOption.IGNORE_CASE)
private val hexColorRegex = Regex("^[0-9a-f]{6}$", RegexOption.IGNORE_CASE)
private val sizeGroupRegex = Regex("^[-+]? *(?:$|\\d+|\\d+\\.\\d*|\\.\\d*) *[a-z]{0,2} *$")
private val sizeRegex = Regex("([-+]?) *(\\d+(?:\\.\\d*)?|\\.\\d+) *([a-z]{2})")
private val urlEscapeRegex = Regex("\\\\([#$%&~_^{}])")
private val verbRegex = Regex("^\\\\verb[^a-zA-Z]")

/**
 * KaTeX's recursive descent parser. The "mouth" is the [Lexer], the "gullet" the
 * [MacroExpander], and this is the "stomach".
 */
class Parser(
    input: String,
    val settings: Settings,
) {
    var mode: Mode = Mode.MATH
    val gullet = MacroExpander(input, settings, mode)
    var leftrightDepth = 0
    private var nextToken: Token? = null

    fun expect(text: String, consume: Boolean = true) {
        if (fetch().text != text) {
            throw ParseError("Expected '$text', got '${fetch().text}'", fetch())
        }
        if (consume) {
            consume()
        }
    }

    fun consume() {
        nextToken = null
    }

    fun fetch(): Token {
        val token = nextToken ?: gullet.expandNextToken()
        nextToken = token
        return token
    }

    fun switchMode(newMode: Mode) {
        mode = newMode
        gullet.switchMode(newMode)
    }

    fun parse(): List<ParseNode> {
        if (!settings.globalGroup) {
            gullet.beginGroup()
        }
        if (settings.colorIsTextColor) {
            gullet.macros.set("\\color", MacroString("\\textcolor"))
        }
        try {
            val parse = parseExpression(false)
            expect("EOF")
            if (!settings.globalGroup) {
                gullet.endGroup()
            }
            return parse
        } finally {
            gullet.endGroups()
        }
    }

    /** Fully parses a separate sequence of tokens as a separate job. */
    fun subparse(tokens: List<Token>): List<ParseNode> {
        val oldToken = nextToken
        consume()
        gullet.pushToken(Token("}"))
        gullet.pushTokens(tokens)
        val parse = parseExpression(false)
        expect("}")
        nextToken = oldToken
        return parse
    }

    fun parseExpression(breakOnInfix: Boolean, breakOnTokenText: String? = null): List<ParseNode> {
        val body = mutableListOf<ParseNode>()
        while (true) {
            if (mode == Mode.MATH) {
                consumeSpaces()
            }
            val lex = fetch()
            if (lex.text in endOfExpression) {
                break
            }
            if (breakOnTokenText != null && lex.text == breakOnTokenText) {
                break
            }
            if (breakOnInfix && Functions.functions[lex.text]?.infix == true) {
                break
            }
            val atom = parseAtom(breakOnTokenText) ?: break
            if (atom is ParseNode.Internal) {
                continue
            }
            body.add(atom)
        }
        if (mode == Mode.TEXT) {
            formLigatures(body)
        }
        return handleInfixNodes(body)
    }

    private fun handleInfixNodes(body: List<ParseNode>): List<ParseNode> {
        var overIndex = -1
        var funcName: String? = null
        body.forEachIndexed { i, node ->
            if (node is ParseNode.Infix) {
                if (overIndex != -1) {
                    throw ParseError("only one infix operator per group", node.token)
                }
                overIndex = i
                funcName = node.replaceWith
            }
        }
        val name = funcName
        if (overIndex == -1 || name == null) {
            return body
        }
        val numerBody = body.subList(0, overIndex)
        val denomBody = body.subList(overIndex + 1, body.size)
        val numerNode = numerBody.singleOrNull() as? ParseNode.OrdGroup ?: ParseNode.OrdGroup(mode, numerBody.toList())
        val denomNode = denomBody.singleOrNull() as? ParseNode.OrdGroup ?: ParseNode.OrdGroup(mode, denomBody.toList())
        val node =
            if (name == "\\\\abovefrac") {
                callFunction(name, listOf(numerNode, body[overIndex], denomNode), emptyList())
            } else {
                callFunction(name, listOf(numerNode, denomNode), emptyList())
            }
        return listOf(node)
    }

    private fun handleSupSubscript(name: String): ParseNode {
        val symbolToken = fetch()
        val symbol = symbolToken.text
        consume()
        consumeSpaces()
        var group: ParseNode?
        do {
            group = parseGroup(name)
        } while (group is ParseNode.Internal)
        return group ?: throw ParseError("Expected group after '$symbol'", symbolToken)
    }

    fun formatUnsupportedCmd(text: String): ParseNode {
        val textordArray = text.map { ParseNode.TextOrd(Mode.TEXT, it.toString()) }
        val textNode = ParseNode.Text(mode, textordArray)
        return ParseNode.Color(mode, settings.errorColor, listOf(textNode))
    }

    private fun parseAtom(breakOnTokenText: String?): ParseNode? {
        val base = parseGroup("atom", breakOnTokenText)
        if (base is ParseNode.Internal) {
            return base
        }
        if (mode == Mode.TEXT) {
            return base
        }

        var superscript: ParseNode? = null
        var subscript: ParseNode? = null
        while (true) {
            consumeSpaces()
            val lex = fetch()
            if (lex.text == "\\limits" || lex.text == "\\nolimits") {
                if (base is ParseNode.Op) {
                    base.limits = lex.text == "\\limits"
                    base.alwaysHandleSupSub = true
                } else if (base is ParseNode.OperatorName) {
                    if (base.alwaysHandleSupSub) {
                        base.limits = lex.text == "\\limits"
                    }
                } else {
                    throw ParseError("Limit controls must follow a math operator", lex)
                }
                consume()
            } else if (lex.text == "^") {
                if (superscript != null) {
                    throw ParseError("Double superscript", lex)
                }
                superscript = handleSupSubscript("superscript")
            } else if (lex.text == "_") {
                if (subscript != null) {
                    throw ParseError("Double subscript", lex)
                }
                subscript = handleSupSubscript("subscript")
            } else if (lex.text == "'") {
                if (superscript != null) {
                    throw ParseError("Double superscript", lex)
                }
                val prime = ParseNode.TextOrd(mode, "\\prime")
                val primes = mutableListOf<ParseNode>(prime)
                consume()
                while (fetch().text == "'") {
                    primes.add(prime)
                    consume()
                }
                if (fetch().text == "^") {
                    primes.add(handleSupSubscript("superscript"))
                }
                superscript = ParseNode.OrdGroup(mode, primes)
            } else if (UnicodeTables.uSubsAndSups.containsKey(lex.text)) {
                val isSub = UnicodeTables.unicodeSubRegex.containsMatchIn(lex.text)
                val subsupTokens = mutableListOf(Token(UnicodeTables.uSubsAndSups.getValue(lex.text)))
                consume()
                while (true) {
                    val token = fetch().text
                    val replacement = UnicodeTables.uSubsAndSups[token] ?: break
                    if (UnicodeTables.unicodeSubRegex.containsMatchIn(token) != isSub) {
                        break
                    }
                    subsupTokens.add(0, Token(replacement))
                    consume()
                }
                val body = subparse(subsupTokens)
                if (isSub) {
                    subscript = ParseNode.OrdGroup(Mode.MATH, body)
                } else {
                    superscript = ParseNode.OrdGroup(Mode.MATH, body)
                }
            } else {
                break
            }
        }

        return if (superscript != null || subscript != null) {
            ParseNode.SupSub(mode, base, sup = superscript, sub = subscript)
        } else {
            base
        }
    }

    fun parseFunction(breakOnTokenText: String? = null, name: String? = null): ParseNode? {
        val token = fetch()
        val func = token.text
        val funcData = Functions.functions[func] ?: return null
        consume()

        if (name != null && name != "atom" && !funcData.allowedInArgument) {
            throw ParseError("Got function '$func' with no arguments as $name", token)
        } else if (mode == Mode.TEXT && !funcData.allowedInText) {
            throw ParseError("Can't use function '$func' in text mode", token)
        } else if (mode == Mode.MATH && !funcData.allowedInMath) {
            throw ParseError("Can't use function '$func' in math mode", token)
        }

        val (args, optArgs) = parseArguments(func, funcData)
        return callFunction(func, args, optArgs, token, breakOnTokenText)
    }

    fun callFunction(
        name: String,
        args: List<ParseNode>,
        optArgs: List<ParseNode?>,
        token: Token? = null,
        breakOnTokenText: String? = null,
    ): ParseNode {
        val context = FunctionContext(funcName = name, parser = this, token = token, breakOnTokenText = breakOnTokenText)
        val handler = Functions.functions[name]?.handler ?: throw ParseError("No function handler for $name")
        return handler(context, args, optArgs)
    }

    fun parseArguments(func: String, funcData: ArgSpec): Pair<List<ParseNode>, List<ParseNode?>> {
        val numOptionalArgs = funcData.numOptionalArgs
        val totalArgs = funcData.numArgs + numOptionalArgs
        if (totalArgs == 0) {
            return emptyList<ParseNode>() to emptyList()
        }
        val args = mutableListOf<ParseNode>()
        val optArgs = mutableListOf<ParseNode?>()

        (0 until totalArgs).forEach { i ->
            var argType = funcData.argTypes?.getOrNull(i)
            val isOptional = i < numOptionalArgs
            val primitive = funcData is FunctionSpec && funcData.primitive
            if ((primitive && argType == null) || (funcData.type == "sqrt" && i == 1 && optArgs[0] == null)) {
                argType = ArgType.PRIMITIVE
            }
            val arg = parseGroupOfType("argument to '$func'", argType, isOptional)
            if (isOptional) {
                optArgs.add(arg)
            } else if (arg != null) {
                args.add(arg)
            } else {
                throw ParseError("Null argument, please report this as a bug")
            }
        }
        return args to optArgs
    }

    private fun parseGroupOfType(name: String, type: ArgType?, optional: Boolean): ParseNode? =
        when (type) {
            ArgType.COLOR -> parseColorGroup(optional)
            ArgType.SIZE -> parseSizeGroup(optional)
            ArgType.URL -> parseUrlGroup(optional)
            ArgType.MATH -> parseArgumentGroup(optional, Mode.MATH)
            ArgType.TEXT -> parseArgumentGroup(optional, Mode.TEXT)
            ArgType.HBOX -> {
                val group = parseArgumentGroup(optional, Mode.TEXT)
                group?.let { ParseNode.Styling(it.mode, style = "text", body = listOf(it), resetFont = true) }
            }
            ArgType.RAW -> {
                gullet.consumeSpaces()
                val percentCatcode = gullet.lexer.catcodes["%"]
                gullet.lexer.setCatcode("%", 12)
                val token = parseStringGroup(optional)
                gullet.lexer.setCatcode("%", percentCatcode)
                token?.let { ParseNode.Raw(Mode.TEXT, it.text) }
            }
            ArgType.PRIMITIVE -> {
                if (optional) {
                    throw ParseError("A primitive argument cannot be optional")
                }
                parseGroup(name) ?: throw ParseError("Expected group as $name", fetch())
            }
            ArgType.ORIGINAL, null -> parseArgumentGroup(optional)
        }

    fun consumeSpaces() {
        while (fetch().text == " ") {
            consume()
        }
    }

    private fun parseStringGroup(optional: Boolean): Token? {
        val argToken = gullet.scanArgument(optional) ?: return null
        val str = StringBuilder()
        var token = fetch()
        while (token.text != "EOF") {
            str.append(token.text)
            consume()
            token = fetch()
        }
        consume()
        argToken.text = str.toString()
        return argToken
    }

    private fun parseRegexGroup(regex: Regex, modeName: String): Token {
        val firstToken = fetch()
        var lastToken = firstToken
        var str = ""
        var token = fetch()
        while (token.text != "EOF" && regex.containsMatchIn(str + token.text)) {
            lastToken = token
            str += lastToken.text
            consume()
            token = fetch()
        }
        if (str.isEmpty()) {
            throw ParseError("Invalid $modeName: '${firstToken.text}'", firstToken)
        }
        return firstToken.range(lastToken, str)
    }

    private fun parseColorGroup(optional: Boolean): ParseNode.ColorToken? {
        val res = parseStringGroup(optional) ?: return null
        val match = colorRegex.find(res.text) ?: throw ParseError("Invalid color: '${res.text}'", res)
        var color = match.value
        if (hexColorRegex.matches(color)) {
            color = "#$color"
        }
        return ParseNode.ColorToken(mode, color)
    }

    fun parseSizeGroup(optional: Boolean): ParseNode.Size? {
        var isBlank = false
        gullet.consumeSpaces()
        val res =
            if (!optional && gullet.future().text != "{") {
                parseRegexGroup(sizeGroupRegex, "size")
            } else {
                parseStringGroup(optional)
            } ?: return null
        if (!optional && res.text.isEmpty()) {
            res.text = "0pt"
            isBlank = true
        }
        val match = sizeRegex.find(res.text) ?: throw ParseError("Invalid size: '${res.text}'", res)
        val data = Measurement((match.groupValues[1] + match.groupValues[2]).toDouble(), match.groupValues[3])
        if (!validUnit(data)) {
            throw ParseError("Invalid unit: '${data.unit}'", res)
        }
        return ParseNode.Size(mode, data, isBlank)
    }

    private fun parseUrlGroup(optional: Boolean): ParseNode.Url? {
        val percentCatcode = gullet.lexer.catcodes["%"]
        val tildeCatcode = gullet.lexer.catcodes["~"]
        gullet.lexer.setCatcode("%", 13)
        gullet.lexer.setCatcode("~", 12)
        val res = parseStringGroup(optional)
        gullet.lexer.setCatcode("%", percentCatcode)
        gullet.lexer.setCatcode("~", tildeCatcode)
        if (res == null) {
            return null
        }
        val url = urlEscapeRegex.replace(res.text) { it.groupValues[1] }
        return ParseNode.Url(mode, url)
    }

    private fun parseArgumentGroup(optional: Boolean, mode: Mode? = null): ParseNode.OrdGroup? {
        val argToken = gullet.scanArgument(optional) ?: return null
        val outerMode = this.mode
        if (mode != null) {
            switchMode(mode)
        }
        gullet.beginGroup()
        val expression = parseExpression(false, "EOF")
        expect("EOF")
        gullet.endGroup()
        val result = ParseNode.OrdGroup(this.mode, expression, loc = argToken.loc)
        if (mode != null) {
            switchMode(outerMode)
        }
        return result
    }

    fun parseGroup(name: String, breakOnTokenText: String? = null): ParseNode? {
        val firstToken = fetch()
        val text = firstToken.text
        if (text == "{" || text == "\\begingroup") {
            consume()
            val groupEnd = if (text == "{") "}" else "\\endgroup"
            gullet.beginGroup()
            val expression = parseExpression(false, groupEnd)
            val lastToken = fetch()
            expect(groupEnd)
            gullet.endGroup()
            return ParseNode.OrdGroup(
                mode,
                expression,
                semisimple = if (text == "\\begingroup") true else null,
                loc = SourceLocation.range(firstToken, lastToken),
            )
        }
        var result = parseFunction(breakOnTokenText, name) ?: parseSymbol()
        if (result == null && text.startsWith("\\") && text !in implicitCommands) {
            if (settings.throwOnError) {
                throw ParseError("Undefined control sequence: $text", firstToken)
            }
            result = formatUnsupportedCmd(text)
            consume()
        }
        return result
    }

    private fun formLigatures(group: MutableList<ParseNode>) {
        var n = group.size - 1
        var i = 0
        while (i < n) {
            val a = group[i]
            val next = group.getOrNull(i + 1)
            if (a is ParseNode.TextOrd && next is ParseNode.TextOrd) {
                val v = a.text
                if (v == "-" && next.text == "-") {
                    val afterNext = group.getOrNull(i + 2)
                    if (i + 1 < n && afterNext is ParseNode.TextOrd && afterNext.text == "-") {
                        repeat(3) { group.removeAt(i) }
                        group.add(i, ParseNode.TextOrd(Mode.TEXT, "---", SourceLocation.range(a, afterNext)))
                        n -= 2
                    } else {
                        repeat(2) { group.removeAt(i) }
                        group.add(i, ParseNode.TextOrd(Mode.TEXT, "--", SourceLocation.range(a, next)))
                        n -= 1
                    }
                }
                if ((v == "'" || v == "`") && next.text == v) {
                    repeat(2) { group.removeAt(i) }
                    group.add(i, ParseNode.TextOrd(Mode.TEXT, v + v, SourceLocation.range(a, next)))
                    n -= 1
                }
            }
            i += 1
        }
    }

    private fun parseSymbol(): ParseNode? {
        val nucleus = fetch()
        var text = nucleus.text

        if (verbRegex.containsMatchIn(text)) {
            consume()
            var arg = text.substring(5)
            val star = arg.firstOrNull() == '*'
            if (star) {
                arg = arg.substring(1)
            }
            if (arg.length < 2 || arg.first() != arg.last()) {
                throw ParseError("\\verb assertion failed -- please report what input caused this bug")
            }
            arg = arg.substring(1, arg.length - 1)
            return ParseNode.Verb(Mode.TEXT, arg, star)
        }

        val first = text.take(1)
        val decomposed = UnicodeTables.unicodeSymbols[first]
        if (decomposed != null && !Symbols[mode].containsKey(first)) {
            text = decomposed + text.substring(1)
        }

        val match = combiningDiacriticalMarksEndRegex.find(text)
        if (match != null) {
            text = text.substring(0, match.range.first)
            if (text == "i") {
                text = "ı"
            } else if (text == "j") {
                text = "ȷ"
            }
        }

        val info = Symbols[mode][text]
        var symbol: ParseNode =
            if (info != null) {
                symbolNode(info.group, mode, text, SourceLocation.range(nucleus))
            } else if (text.isNotEmpty() && text[0].code >= 0x80) {
                ParseNode.TextOrd(Mode.TEXT, text, SourceLocation.range(nucleus))
            } else {
                return null
            }
        consume()

        match?.value?.forEach { accentChar ->
            val accent = accentChar.toString()
            val commands = UnicodeTables.unicodeAccents[accent] ?: throw ParseError("Unknown accent ' $accent'", nucleus)
            val command =
                (if (mode == Mode.MATH) commands.math else commands.text) ?: commands.text
                    ?: throw ParseError("Accent $accent unsupported in ${mode.key} mode", nucleus)
            symbol =
                ParseNode.Accent(
                    mode,
                    label = command,
                    base = symbol,
                    isStretchy = false,
                    isShifty = true,
                    loc = SourceLocation.range(nucleus),
                )
        }
        return symbol
    }
}
