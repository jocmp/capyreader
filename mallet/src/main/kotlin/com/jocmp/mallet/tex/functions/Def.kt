package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.Functions
import com.jocmp.mallet.tex.MacroExpansion
import com.jocmp.mallet.tex.Mode
import com.jocmp.mallet.tex.ParseError
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.Parser
import com.jocmp.mallet.tex.Token
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.defineFunction

private val globalMap =
    mapOf(
        "\\global" to "\\global",
        "\\long" to "\\\\globallong",
        "\\\\globallong" to "\\\\globallong",
        "\\def" to "\\gdef",
        "\\gdef" to "\\gdef",
        "\\edef" to "\\xdef",
        "\\xdef" to "\\xdef",
        "\\let" to "\\\\globallet",
        "\\futurelet" to "\\\\globalfuture",
    )

private val controlSequenceRegex = Regex("^(?:[\\\\{}$&#^_]|EOF)$")

private val argumentNumberRegex = Regex("^[1-9]$")

internal fun defineDef() {
    defineFunction(
        type = "internal",
        names = listOf("\\global", "\\long", "\\\\globallong"),
        numArgs = 0,
        allowedInText = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val funcName = context.funcName
            parser.consumeSpaces()
            val token = parser.fetch()
            val global = globalMap[token.text]
            if (!global.isNullOrEmpty()) {
                if (funcName == "\\global" || funcName == "\\\\globallong") {
                    token.text = global
                }
                assertNodeType<ParseNode.Internal>(parser.parseFunction())
            } else {
                throw ParseError("Invalid token after macro prefix", token)
            }
        },
    )

    defineFunction(
        type = "internal",
        names = listOf("\\def", "\\gdef", "\\edef", "\\xdef"),
        numArgs = 0,
        allowedInText = true,
        primitive = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val funcName = context.funcName
            var tok = parser.gullet.popToken()
            val name = tok.text
            if (controlSequenceRegex.containsMatchIn(name)) {
                throw ParseError("Expected a control sequence", tok)
            }

            var numArgs = 0
            var insert: Token? = null
            val delimiters = mutableListOf<MutableList<String>>(mutableListOf())
            while (parser.gullet.future().text != "{") {
                tok = parser.gullet.popToken()
                if (tok.text == "#") {
                    if (parser.gullet.future().text == "{") {
                        insert = parser.gullet.future()
                        delimiters[numArgs].add("{")
                        break
                    }

                    tok = parser.gullet.popToken()
                    if (!argumentNumberRegex.containsMatchIn(tok.text)) {
                        throw ParseError("Invalid argument number \"${tok.text}\"")
                    }
                    if (tok.text.toInt() != numArgs + 1) {
                        throw ParseError("Argument number \"${tok.text}\" out of order")
                    }
                    numArgs += 1
                    delimiters.add(mutableListOf())
                } else if (tok.text == "EOF") {
                    throw ParseError("Expected a macro definition")
                } else {
                    delimiters[numArgs].add(tok.text)
                }
            }
            var tokens: MutableList<Token> = parser.gullet.consumeArg().tokens
            if (insert != null) {
                tokens.add(0, insert)
            }

            if (funcName == "\\edef" || funcName == "\\xdef") {
                tokens = parser.gullet.expandTokens(tokens).toMutableList()
                tokens.reverse()
            }
            parser.gullet.macros.set(
                name,
                MacroExpansion(tokens = tokens, numArgs = numArgs, delimiters = delimiters),
                funcName == globalMap[funcName],
            )

            ParseNode.Internal(parser.mode)
        },
    )

    defineFunction(
        type = "internal",
        names = listOf("\\let", "\\\\globallet"),
        numArgs = 0,
        allowedInText = true,
        primitive = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val name = checkControlSequence(parser.gullet.popToken())
            parser.gullet.consumeSpaces()
            val tok = getRHS(parser)
            letCommand(parser, name, tok, context.funcName == "\\\\globallet")
            ParseNode.Internal(parser.mode)
        },
    )

    defineFunction(
        type = "internal",
        names = listOf("\\futurelet", "\\\\globalfuture"),
        numArgs = 0,
        allowedInText = true,
        primitive = true,
        handler = { context, _, _ ->
            val parser = context.parser
            val name = checkControlSequence(parser.gullet.popToken())
            val middle = parser.gullet.popToken()
            val tok = parser.gullet.popToken()
            letCommand(parser, name, tok, context.funcName == "\\\\globalfuture")
            parser.gullet.pushToken(tok)
            parser.gullet.pushToken(middle)
            ParseNode.Internal(parser.mode)
        },
    )
}

private fun checkControlSequence(tok: Token): String {
    val name = tok.text
    if (controlSequenceRegex.containsMatchIn(name)) {
        throw ParseError("Expected a control sequence", tok)
    }
    return name
}

private fun getRHS(parser: Parser): Token {
    var tok = parser.gullet.popToken()
    if (tok.text == "=") {
        tok = parser.gullet.popToken()
        if (tok.text == " ") {
            tok = parser.gullet.popToken()
        }
    }
    return tok
}

private fun letCommand(parser: Parser, name: String, tok: Token, global: Boolean) {
    var macro = parser.gullet.macros.get(tok.text)
    if (macro == null) {
        tok.noexpand = true
        macro =
            MacroExpansion(
                tokens = listOf(tok),
                numArgs = 0,
                unexpandable = !parser.gullet.isExpandable(tok.text),
            )
    }
    parser.gullet.macros.set(name, macro, global)
}
