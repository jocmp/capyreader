package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.Options
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.buildExpression
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.ordargument

private val textFontFamilies: Map<String, String?> =
    mapOf(
        "\\text" to null,
        "\\textrm" to "textrm",
        "\\textsf" to "textsf",
        "\\texttt" to "texttt",
        "\\textnormal" to "textrm",
    )

private val textFontWeights =
    mapOf(
        "\\textbf" to "textbf",
        "\\textmd" to "textmd",
    )

private val textFontShapes =
    mapOf(
        "\\textit" to "textit",
        "\\textup" to "textup",
    )

internal fun defineText() {
    defineFunction(
        type = "text",
        names =
            listOf(
                "\\text", "\\textrm", "\\textsf", "\\texttt", "\\textnormal",
                "\\textbf", "\\textmd",
                "\\textit", "\\textup", "\\emph",
            ),
        numArgs = 1,
        argTypes = listOf(ArgType.TEXT),
        allowedInArgument = true,
        allowedInText = true,
        handler = { context, args, _ ->
            val body = args[0]
            ParseNode.Text(
                mode = context.parser.mode,
                body = ordargument(body),
                font = context.funcName,
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Text>(group)
            val newOptions = optionsWithFont(node, options)
            val inner = buildExpression(node.body, newOptions, true)
            makeSpan(listOf("mord", "text"), inner, newOptions)
        },
    )
}

private fun optionsWithFont(group: ParseNode.Text, options: Options): Options {
    val font = group.font
    if (font.isNullOrEmpty()) {
        return options
    }
    val family = textFontFamilies[font]
    val weight = textFontWeights[font]
    return if (family != null) {
        options.withTextFontFamily(family)
    } else if (weight != null) {
        options.withTextFontWeight(weight)
    } else if (font == "\\emph") {
        if (options.fontShape == "textit") {
            options.withTextFontShape("textup")
        } else {
            options.withTextFontShape("textit")
        }
    } else {
        options.withTextFontShape(textFontShapes[font].orEmpty())
    }
}
