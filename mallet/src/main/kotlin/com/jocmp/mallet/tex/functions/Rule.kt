package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.ArgType
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.calculateSize
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeEm
import com.jocmp.mallet.tex.makeSpan

internal fun defineRule() {
    defineFunction(
        type = "rule",
        names = listOf("\\rule"),
        numArgs = 2,
        numOptionalArgs = 1,
        allowedInText = true,
        allowedInMath = true,
        argTypes = listOf(ArgType.SIZE, ArgType.SIZE, ArgType.SIZE),
        handler = { context, args, optArgs ->
            val shift = optArgs.getOrNull(0)
            val width = assertNodeType<ParseNode.Size>(args[0])
            val height = assertNodeType<ParseNode.Size>(args[1])
            ParseNode.Rule(
                mode = context.parser.mode,
                shift = shift?.let { assertNodeType<ParseNode.Size>(it).value },
                width = width.value,
                height = height.value,
            )
        },
        htmlBuilder = { group, options ->
            val node = assertNodeType<ParseNode.Rule>(group)
            val rule = makeSpan(listOf("mord", "katex-rule"), emptyList(), options)

            val width = calculateSize(node.width, options)
            val height = calculateSize(node.height, options)
            val shift = if (node.shift != null) calculateSize(node.shift, options) else 0.0

            rule.style.borderRightWidth = makeEm(width)
            rule.style.borderTopWidth = makeEm(height)
            rule.style.bottom = makeEm(shift)

            rule.width = width
            rule.height = height + shift
            rule.depth = -shift
            rule.maxFontSize = height * 1.125 * options.sizeMultiplier

            rule
        },
    )
}
