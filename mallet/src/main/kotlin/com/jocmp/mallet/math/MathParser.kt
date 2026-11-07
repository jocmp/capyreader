package com.jocmp.mallet.math

/**
 * Thrown for TeX that mallet can't parse, including commands outside its KaTeX subset.
 */
class MathParseException(message: String) : Exception(message)

/**
 * Parses KaTeX-flavored TeX math into a [MathList].
 */
internal class MathParser(source: String) {
    private var chars: String = source
    private var index = 0
    private var currentInner: Inner? = null
    private var currentEnv: Environment? = null
    private var currentFontStyle = FontStyle.DEFAULT
    private var textMode = false

    fun parse(): MathList {
        val list = buildList(oneCharOnly = false, stopChar = null)
        if (hasCharacters()) {
            throw MathParseException("Mismatched braces")
        }
        return list
    }

    private fun buildList(oneCharOnly: Boolean, stopChar: Char?): MathList {
        val list: MathList = mutableListOf()
        var prevAtom: MathAtom? = null

        while (hasCharacters()) {
            val ch = nextChar()

            if (oneCharOnly && (ch == '^' || ch == '}' || ch == '_' || ch == '&')) {
                // Left for the caller.
                unread()
                return list
            }
            if (stopChar != null && ch == stopChar) {
                return list
            }

            val atom: MathAtom =
                when {
                    ch == '^' && !textMode -> {
                        val base = scriptBase(list, prevAtom) { it.superscript == null || it.superscript.isPrimes() }
                        val script = buildList(oneCharOnly = true, stopChar = null)
                        base.superscript = base.superscript?.apply { addAll(script) } ?: script
                        prevAtom = base
                        continue
                    }

                    ch == '_' && !textMode -> {
                        val base = scriptBase(list, prevAtom) { it.subscript == null }
                        base.subscript = buildList(oneCharOnly = true, stopChar = null)
                        prevAtom = base
                        continue
                    }

                    ch == '\'' && !textMode -> {
                        val base = scriptBase(list, prevAtom) { it.superscript == null || it.superscript.isPrimes() }
                        val prime = MathAtom(AtomType.ORDINARY, PRIME)
                        base.superscript = base.superscript?.apply { add(prime) } ?: mutableListOf(prime)
                        prevAtom = base
                        continue
                    }

                    ch == '{' -> {
                        val sublist = buildList(oneCharOnly = false, stopChar = '}')
                        prevAtom = sublist.lastOrNull()
                        list.addAll(sublist)
                        if (oneCharOnly) {
                            return list
                        }
                        continue
                    }

                    ch == '}' -> throw MathParseException("Mismatched braces")

                    ch == '%' -> {
                        skipComment()
                        continue
                    }

                    ch == '&' -> {
                        if (currentEnv != null) {
                            return list
                        }
                        return mutableListOf(buildTable(environment = null, firstList = list, isRow = false))
                    }

                    ch == '\\' -> {
                        val command = readCommand()

                        stopCommand(command, list, stopChar)?.let { return it }

                        if (applyModifier(command, prevAtom)) {
                            continue
                        }

                        if (command == "color") {
                            val argb = readColor()
                            list.add(MathColor(argb, buildList(oneCharOnly = false, stopChar = stopChar)))
                            return list
                        }

                        val fontStyle = MathSymbols.fontStyle(command)
                        if (fontStyle != null) {
                            val sublist = withFontStyle(fontStyle, text = MathSymbols.isTextCommand(command)) {
                                buildList(oneCharOnly = true, stopChar = null)
                            }
                            prevAtom = sublist.lastOrNull()
                            list.addAll(sublist)
                            if (oneCharOnly) {
                                return list
                            }
                            continue
                        }

                        atomForCommand(command) ?: continue
                    }

                    ch.isWhitespace() -> {
                        if (!textMode) {
                            continue
                        }
                        MathAtom(AtomType.ORDINARY, NBSP)
                    }

                    ch == '~' -> MathSpace(INTERWORD_SPACE)

                    // \text{$k$ times} switches back to math mode between the dollars.
                    ch == '$' && textMode -> {
                        val math = withFontStyle(FontStyle.DEFAULT, text = false) {
                            buildList(oneCharOnly = false, stopChar = '$')
                        }
                        prevAtom = math.lastOrNull()
                        list.addAll(math)
                        continue
                    }

                    textMode -> textAtom(readCodepoint(ch))

                    ch.code > 0x7E -> MathSymbols.atomForCodepoint(readCodepoint(ch))

                    else -> atomForCharacter(ch) ?: continue
                }

            atom.fontStyle = currentFontStyle
            list.add(atom)
            prevAtom = atom

            if (oneCharOnly) {
                return list
            }
        }

        if (stopChar == '}') {
            throw MathParseException("Missing closing brace")
        } else if (stopChar != null) {
            throw MathParseException("Expected character not found: $stopChar")
        }
        return list
    }

    private fun atomForCommand(command: String): MathAtom? {
        MathSymbols.atom(command)?.let { return it }

        MathSymbols.accent(command)?.let { nucleus ->
            return Accent(nucleus, buildList(oneCharOnly = true, stopChar = null))
        }

        bigDelimiters[command]?.let { (type, height) ->
            val delimiter = readDelimiter() ?: throw MathParseException("Missing delimiter for \\$command")
            val value = MathSymbols.delimiter(delimiter) ?: throw MathParseException("Invalid delimiter $delimiter")
            return SizedDelimiter(type = type, delimiter = value, height = height)
        }

        return when (command) {
            "frac", "cfrac" -> fraction(hasRule = true)
            "dfrac" -> styled(LineStyle.DISPLAY, fraction(hasRule = true))
            "tfrac" -> styled(LineStyle.TEXT, fraction(hasRule = true))
            "binom" -> fraction(hasRule = false, left = "(", right = ")")
            "dbinom" -> styled(LineStyle.DISPLAY, fraction(hasRule = false, left = "(", right = ")"))
            "tbinom" -> styled(LineStyle.TEXT, fraction(hasRule = false, left = "(", right = ")"))
            "sqrt" -> radical()
            "left" -> leftRight()
            "overline" -> Overline(buildList(oneCharOnly = true, stopChar = null))
            "underline" -> Underline(buildList(oneCharOnly = true, stopChar = null))
            "boxed" -> Framed(buildList(oneCharOnly = true, stopChar = null))
            "overbrace" -> Brace(buildList(oneCharOnly = true, stopChar = null), over = true)
            "underbrace" -> Brace(buildList(oneCharOnly = true, stopChar = null), over = false)
            "begin" -> buildTable(environment = readEnvironment(), firstList = null, isRow = false)
            "textcolor" -> MathColor(readColor(), buildList(oneCharOnly = true, stopChar = null))
            "operatorname" -> operatorName(limits = false)
            "operatorname*", "operatornamewithlimits" -> operatorName(limits = true)
            "mathop" -> retyped(AtomType.LARGE_OPERATOR)
            "mathbin" -> retyped(AtomType.BINARY_OPERATOR)
            "mathrel" -> retyped(AtomType.RELATION)
            "mathord" -> retyped(AtomType.ORDINARY)
            "mathopen" -> retyped(AtomType.OPEN)
            "mathclose" -> retyped(AtomType.CLOSE)
            "mathpunct" -> retyped(AtomType.PUNCTUATION)
            "mathinner" -> Inner(buildList(oneCharOnly = true, stopChar = null))
            "not" -> negate()
            "overset" -> {
                val over = buildList(oneCharOnly = true, stopChar = null)
                stack(over = over, under = null)
            }

            "underset" -> {
                val under = buildList(oneCharOnly = true, stopChar = null)
                stack(over = null, under = under)
            }

            "stackrel" -> {
                val over = buildList(oneCharOnly = true, stopChar = null)
                val base = buildList(oneCharOnly = true, stopChar = null)
                Stack(AtomType.RELATION, base, over, null)
            }

            "xrightarrow" -> extensibleArrow("⟶")
            "xleftarrow" -> extensibleArrow("⟵")
            "pmod" -> {
                val argument = readGroup()
                expand("\\mkern8mu(\\mathrm{mod}\\mkern6mu $argument)")
                null
            }

            "pod" -> {
                val argument = readGroup()
                expand("\\mkern8mu($argument)")
                null
            }

            "mkern", "mskip", "kern", "hskip" -> MathSpace(readDimension(braced = false))
            "hspace", "hspace*" -> MathSpace(readDimension(braced = true))
            "tag", "tag*", "label" -> {
                readGroup()
                null
            }

            "phantom", "hphantom", "vphantom" -> {
                buildList(oneCharOnly = true, stopChar = null)
                null
            }

            // Table rules aren't drawn; the cells still lay out.
            "hline", "hdashline" -> null
            "nonumber", "notag", "nolinebreak", "allowbreak", "relax" -> null
            else -> throw MathParseException("Unknown command \\$command")
        }
    }

    private fun stopCommand(command: String, list: MathList, stopChar: Char?): MathList? {
        return when (command) {
            "right" -> {
                val inner = currentInner ?: throw MathParseException("Missing \\left")
                inner.rightBoundary = readBoundary("right")
                list
            }

            "over", "atop", "choose", "brack", "brace" -> {
                val delimiters = fractionCommands.getValue(command)
                val frac =
                    Fraction(
                        numerator = list,
                        denominator = buildList(oneCharOnly = false, stopChar = stopChar),
                        hasRule = command == "over",
                        leftDelimiter = delimiters?.first,
                        rightDelimiter = delimiters?.second,
                    )
                mutableListOf(frac)
            }

            "\\", "cr", "newline" -> {
                // An optional spacing argument, as in \\[4pt].
                skipOptionalArgument()
                val env = currentEnv
                if (env != null) {
                    env.numRows += 1
                    list
                } else {
                    mutableListOf(buildTable(environment = null, firstList = list, isRow = true))
                }
            }

            "end" -> {
                val env = currentEnv ?: throw MathParseException("Missing \\begin")
                val name = readEnvironment()
                if (name != env.name) {
                    throw MathParseException("\\begin{${env.name}} does not match \\end{$name}")
                }
                env.ended = true
                list
            }

            "middle" -> throw MathParseException("Unsupported command \\middle")
            else -> null
        }
    }

    private fun applyModifier(modifier: String, atom: MathAtom?): Boolean {
        val limits =
            when (modifier) {
                "limits" -> true
                "nolimits" -> false
                "displaylimits" -> true
                else -> return false
            }

        (atom as? LargeOperator)?.hasLimits = limits
        return true
    }

    private fun scriptBase(list: MathList, prevAtom: MathAtom?, accepts: (MathAtom) -> Boolean): MathAtom {
        if (prevAtom != null && prevAtom.type.allowsScripts && accepts(prevAtom)) {
            return prevAtom
        }
        return MathAtom(AtomType.ORDINARY, "").also { list.add(it) }
    }

    private fun fraction(
        hasRule: Boolean,
        left: String? = null,
        right: String? = null,
    ): Fraction =
        Fraction(
            numerator = buildList(oneCharOnly = true, stopChar = null),
            denominator = buildList(oneCharOnly = true, stopChar = null),
            hasRule = hasRule,
            leftDelimiter = left,
            rightDelimiter = right,
        )

    private fun styled(style: LineStyle, atom: MathAtom): Inner = Inner(mutableListOf(MathStyle(style), atom))

    private fun radical(): Radical {
        skipSpaces()
        if (hasCharacters() && peek() == '[') {
            nextChar()
            val degree = buildList(oneCharOnly = false, stopChar = ']')
            return Radical(radicand = buildList(oneCharOnly = true, stopChar = null), degree = degree)
        }
        return Radical(radicand = buildList(oneCharOnly = true, stopChar = null))
    }

    private fun leftRight(): Inner {
        val oldInner = currentInner
        val inner = Inner(leftBoundary = readBoundary("left"))
        currentInner = inner
        inner.innerList = buildList(oneCharOnly = false, stopChar = null)
        if (inner.rightBoundary == null) {
            throw MathParseException("Missing \\right")
        }
        currentInner = oldInner
        return inner
    }

    private fun operatorName(limits: Boolean): LargeOperator {
        val name =
            readGroup()
                .replace("\\,", " ")
                .replace("\\ ", " ")
                .replace("\\!", "")
        if (name.contains('\\') || name.isBlank()) {
            throw MathParseException("Unsupported operator name $name")
        }
        return LargeOperator(name.trim(), limits)
    }

    private fun retyped(type: AtomType): MathAtom {
        val group = buildList(oneCharOnly = true, stopChar = null)
        val single = group.singleOrNull()
        if (single == null || single::class != MathAtom::class) {
            return Inner(group)
        }
        if (type == AtomType.LARGE_OPERATOR) {
            return LargeOperator(single.nucleus, hasLimits = true)
        }
        // Variables and numbers already space as ordinary and keep their italic or upright style.
        if (type == AtomType.ORDINARY && (single.type == AtomType.VARIABLE || single.type == AtomType.NUMBER)) {
            return single
        }
        single.type = type
        return single
    }

    private fun negate(): MathAtom {
        val group = buildList(oneCharOnly = true, stopChar = null)
        val atom = group.singleOrNull() ?: throw MathParseException("\\not needs a single symbol")
        val negated = MathSymbols.negated(atom.nucleus) ?: throw MathParseException("Can't negate ${atom.nucleus}")
        atom.nucleus = negated
        return atom
    }

    private fun stack(over: MathList?, under: MathList?): Stack {
        val base = buildList(oneCharOnly = true, stopChar = null)
        val type = base.singleOrNull()?.type?.takeIf { it == AtomType.RELATION || it == AtomType.BINARY_OPERATOR }
        return Stack(type ?: AtomType.ORDINARY, base, over, under)
    }

    private fun extensibleArrow(arrow: String): Stack {
        skipOptionalArgument()
        val over = buildList(oneCharOnly = true, stopChar = null)
        return Stack(AtomType.RELATION, mutableListOf(MathAtom(AtomType.RELATION, arrow)), over, null)
    }

    private fun readBoundary(command: String): String {
        val delimiter = readDelimiter() ?: throw MathParseException("Missing delimiter for \\$command")
        return MathSymbols.delimiter(delimiter) ?: throw MathParseException("Invalid delimiter for \\$command: $delimiter")
    }

    private fun buildTable(environment: String?, firstList: MathList?, isRow: Boolean): MathAtom {
        val oldEnv = currentEnv
        val env = Environment(environment)
        currentEnv = env

        val columnSpec = if (environment in setOf("array", "darray")) readColumnSpec() else null
        if (environment in setOf("alignat", "alignat*", "alignedat")) {
            readGroup()
        }

        var currentRow = 0
        val rows: MutableList<MutableList<MathList>> = mutableListOf(mutableListOf())

        if (firstList != null) {
            rows[currentRow].add(firstList)
            if (isRow) {
                env.numRows += 1
                currentRow += 1
                rows.add(mutableListOf())
            }
        }

        while (!env.ended && hasCharacters()) {
            val list = buildList(oneCharOnly = false, stopChar = null)
            rows[currentRow].add(list)
            if (env.numRows > currentRow) {
                currentRow = env.numRows
                rows.add(mutableListOf())
            }
        }

        if (!env.ended && environment != null) {
            throw MathParseException("Missing \\end{$environment}")
        }

        // A trailing \\ leaves an empty last row.
        val last = rows.last()
        if (rows.size > 1 && (last.isEmpty() || (last.size == 1 && last[0].isEmpty()))) {
            rows.removeAt(rows.lastIndex)
        }

        currentEnv = oldEnv
        return MathEnvironments.table(environment, rows, columnSpec)
    }

    private fun atomForCharacter(ch: Char): MathAtom? {
        val value = ch.toString()
        return when {
            ch.code < 0x21 || ch.code > 0x7E -> null
            ch == '$' || ch == '#' -> null
            ch == '(' || ch == '[' -> MathAtom(AtomType.OPEN, value)
            ch == ')' || ch == ']' || ch == '!' || ch == '?' -> MathAtom(AtomType.CLOSE, value)
            ch == ',' || ch == ';' -> MathAtom(AtomType.PUNCTUATION, value)
            ch == '=' || ch == '>' || ch == '<' -> MathAtom(AtomType.RELATION, value)
            // Math colon is a ratio relation. \colon is punctuation.
            ch == ':' -> MathAtom(AtomType.RELATION, "∶")
            ch == '-' -> MathAtom(AtomType.BINARY_OPERATOR, "−")
            ch == '+' || ch == '*' -> MathAtom(AtomType.BINARY_OPERATOR, if (ch == '*') "∗" else value)
            ch == '.' || ch.isAsciiDigit() -> MathAtom(AtomType.NUMBER, value)
            ch.isAsciiLetter() -> MathAtom(AtomType.VARIABLE, value)
            else -> MathAtom(AtomType.ORDINARY, value)
        }
    }

    private fun textAtom(value: String): MathAtom {
        val ch = value.singleOrNull()
        return when {
            ch != null && ch.isAsciiLetter() -> MathAtom(AtomType.VARIABLE, value)
            ch != null && ch.isAsciiDigit() -> MathAtom(AtomType.NUMBER, value)
            else -> MathAtom(AtomType.ORDINARY, value)
        }
    }

    private inline fun <T> withFontStyle(style: FontStyle, text: Boolean, block: () -> T): T {
        val oldStyle = currentFontStyle
        val oldTextMode = textMode
        currentFontStyle = style
        textMode = text
        return try {
            block()
        } finally {
            currentFontStyle = oldStyle
            textMode = oldTextMode
        }
    }

    private fun readCommand(): String {
        if (!hasCharacters()) {
            throw MathParseException("Trailing backslash")
        }

        val ch = nextChar()
        if (!ch.isAsciiLetter()) {
            return if (ch.isWhitespace()) " " else ch.toString()
        }

        unread()
        val name = StringBuilder()
        while (hasCharacters() && peek().isAsciiLetter()) {
            name.append(nextChar())
        }
        if (hasCharacters() && peek() == '*' && "$name*" in starredCommands) {
            name.append(nextChar())
        }
        // TeX skips spaces after a control word.
        skipSpaces()
        return name.toString()
    }

    private fun readDelimiter(): String? {
        skipSpaces()
        if (!hasCharacters()) {
            return null
        }

        val ch = nextChar()
        if (ch != '\\') {
            return ch.toString()
        }

        val command = readCommand()
        // \| is both a command and a delimiter.
        return if (command == "|") "||" else command
    }

    private fun readEnvironment(): String {
        expect('{')
        val name = StringBuilder()
        while (hasCharacters() && (peek().isAsciiLetter() || peek() == '*')) {
            name.append(nextChar())
        }
        expect('}')
        return name.toString()
    }

    private fun readColumnSpec(): List<ColumnAlignment> {
        val spec = readGroup()
        return spec.mapNotNull { ch ->
            when (ch) {
                'l' -> ColumnAlignment.LEFT
                'c' -> ColumnAlignment.CENTER
                'r' -> ColumnAlignment.RIGHT
                else -> null
            }
        }
    }

    private fun readColor(): Int {
        val value = readGroup().trim()
        return MathSymbols.color(value) ?: throw MathParseException("Unknown color $value")
    }

    /**
     * Reads a braced argument verbatim, or a single character when unbraced.
     */
    private fun readGroup(): String {
        skipSpaces()
        if (!hasCharacters()) {
            throw MathParseException("Missing argument")
        }
        if (peek() != '{') {
            return nextChar().toString()
        }

        nextChar()
        val start = index
        var depth = 1
        while (hasCharacters()) {
            when (nextChar()) {
                '\\' -> if (hasCharacters()) nextChar()
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return chars.substring(start, index - 1)
                    }
                }
            }
        }
        throw MathParseException("Missing closing brace")
    }

    /**
     * Reads a TeX dimension and converts it to mu.
     */
    private fun readDimension(braced: Boolean): Float {
        val source = if (braced) readGroup() else readDimensionText()
        val match =
            dimensionPattern.matchEntire(source.trim())
                ?: throw MathParseException("Invalid dimension $source")
        val amount = match.groupValues[1].toFloat()
        val muPerUnit = muPerUnit[match.groupValues[2]] ?: throw MathParseException("Unknown unit ${match.groupValues[2]}")
        return amount * muPerUnit
    }

    private fun readDimensionText(): String {
        skipSpaces()
        val text = StringBuilder()
        while (hasCharacters() && (peek().isDigit() || peek() in "+-.")) {
            text.append(nextChar())
        }
        skipSpaces()
        repeat(2) {
            if (hasCharacters() && peek().isAsciiLetter()) {
                text.append(nextChar())
            }
        }
        return text.toString()
    }

    private fun skipOptionalArgument() {
        skipSpaces()
        if (!hasCharacters() || peek() != '[') {
            return
        }
        while (hasCharacters() && nextChar() != ']') {
            // Consume until the closing bracket.
        }
    }

    private fun expand(replacement: String) {
        chars = chars.substring(0, index) + replacement + chars.substring(index)
    }

    private fun expect(ch: Char) {
        skipSpaces()
        if (!hasCharacters() || nextChar() != ch) {
            throw MathParseException("Expected $ch")
        }
    }

    private fun skipSpaces() {
        while (hasCharacters() && peek().isWhitespace()) {
            nextChar()
        }
    }

    private fun skipComment() {
        while (hasCharacters() && nextChar() != '\n') {
            // Consume the rest of the line.
        }
    }

    private fun readCodepoint(first: Char): String {
        if (first.isHighSurrogate() && hasCharacters()) {
            return "$first${nextChar()}"
        }
        return first.toString()
    }

    private fun hasCharacters(): Boolean = index < chars.length

    private fun peek(): Char = chars[index]

    private fun nextChar(): Char {
        val ch = chars[index]
        index += 1
        return ch
    }

    private fun unread() {
        index -= 1
    }

    private class Environment(val name: String?) {
        var ended = false
        var numRows = 0
    }

    companion object {
        private const val PRIME = "′"
        private const val NBSP = " "
        private const val INTERWORD_SPACE = 6f

        private val fractionCommands: Map<String, Pair<String, String>?> =
            mapOf(
                "over" to null,
                "atop" to null,
                "choose" to ("(" to ")"),
                "brack" to ("[" to "]"),
                "brace" to ("{" to "}"),
            )

        // Heights in em, from KaTeX's sizeToMaxHeight.
        private val bigDelimiters: Map<String, Pair<AtomType, Float>> =
            buildMap {
                listOf("big" to 1.2f, "Big" to 1.8f, "bigg" to 2.4f, "Bigg" to 3.0f).forEach { (name, height) ->
                    put(name, AtomType.ORDINARY to height)
                    put("${name}l", AtomType.OPEN to height)
                    put("${name}r", AtomType.CLOSE to height)
                    put("${name}m", AtomType.RELATION to height)
                }
            }

        private val starredCommands = setOf("operatorname*", "hspace*", "tag*")

        private val dimensionPattern = Regex("""([+-]?\d*\.?\d+)\s*([a-z]{2})""")

        // KaTeX measures pt against a 10pt font, so 1em = 10pt = 18mu.
        private val muPerUnit =
            mapOf(
                "mu" to 1f,
                "em" to 18f,
                "ex" to 18f * 0.431f,
                "pt" to 1.8f,
                "px" to 1.8f,
            )
    }
}

private fun MathList?.isPrimes(): Boolean = this != null && isNotEmpty() && all { it.nucleus == "′" }

private fun Char.isAsciiLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
