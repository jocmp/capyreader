package com.jocmp.mallet.tex.functions

import com.jocmp.mallet.tex.HtmlDomNode
import com.jocmp.mallet.tex.ParseNode
import com.jocmp.mallet.tex.VListChild
import com.jocmp.mallet.tex.VListParam
import com.jocmp.mallet.tex.assertNodeType
import com.jocmp.mallet.tex.assertSymbolNodeType
import com.jocmp.mallet.tex.buildGroup
import com.jocmp.mallet.tex.defineFunction
import com.jocmp.mallet.tex.makeSpan
import com.jocmp.mallet.tex.makeVList

internal fun defineSmash() {
    defineFunction(
        type = "smash",
        names = listOf("\\smash"),
        numArgs = 1,
        numOptionalArgs = 1,
        allowedInText = true,
        handler = { context, args, optArgs ->
            var smashHeight = false
            var smashDepth = false
            val tbArg = optArgs.getOrNull(0)?.let { assertNodeType<ParseNode.OrdGroup>(it) }
            if (tbArg != null) {
                for (node in tbArg.body) {
                    val letter = assertSymbolNodeType(node).text
                    if (letter == "t") {
                        smashHeight = true
                    } else if (letter == "b") {
                        smashDepth = true
                    } else {
                        smashHeight = false
                        smashDepth = false
                        break
                    }
                }
            } else {
                smashHeight = true
                smashDepth = true
            }

            val body = args[0]
            ParseNode.Smash(
                mode = context.parser.mode,
                body = body,
                smashHeight = smashHeight,
                smashDepth = smashDepth,
            )
        },
        htmlBuilder = { group, options ->
            val smash = assertNodeType<ParseNode.Smash>(group)
            val node = makeSpan(emptyList(), listOf(buildGroup(smash.body, options)))

            if (!smash.smashHeight && !smash.smashDepth) {
                return@defineFunction node
            }

            if (smash.smashHeight) {
                node.height = 0.0
            }

            if (smash.smashDepth) {
                node.depth = 0.0
            }

            if (smash.smashHeight && smash.smashDepth) {
                return@defineFunction makeSpan(listOf("mord", "katex-smash"), listOf(node), options)
            }

            node.children.filterIsInstance<HtmlDomNode>().forEach { child ->
                if (smash.smashHeight) {
                    child.height = 0.0
                }
                if (smash.smashDepth) {
                    child.depth = 0.0
                }
            }

            val smashedNode =
                makeVList(
                    VListParam.FirstBaseline(children = listOf(VListChild.Elem(elem = node))),
                    options,
                )

            makeSpan(listOf("mord"), listOf(smashedNode), options)
        },
    )
}
