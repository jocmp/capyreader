package com.jocmp.mallet.tex

private val binLeftCanceller = setOf("leftmost", "mbin", "mopen", "mrel", "mop", "mpunct")
private val binRightCanceller = setOf("rightmost", "mrel", "mclose", "mpunct")

val styleMap: Map<String, Style> =
    mapOf(
        "display" to Style.DISPLAY,
        "text" to Style.TEXT,
        "script" to Style.SCRIPT,
        "scriptscript" to Style.SCRIPTSCRIPT,
    )

private val domTypes = setOf("mord", "mop", "mbin", "mrel", "mopen", "mclose", "mpunct", "minner")

/**
 * Builds a list of nodes, inserting inter-atom spacing when [isRealGroup] is true or [root].
 * [surrounding] gives the atom types just outside the group, for bin cancelling.
 */
fun buildExpression(
    expression: List<ParseNode>,
    options: Options,
    isRealGroup: Boolean,
    surrounding: Pair<String?, String?> = null to null,
    root: Boolean = false,
): MutableList<HtmlDomNode> {
    val groups = mutableListOf<HtmlDomNode>()
    expression.forEach { node ->
        val output = buildGroup(node, options)
        if (output is DocumentFragment) {
            groups.addAll(output.children)
        } else {
            groups.add(output)
        }
    }
    tryCombineChars(groups)

    if (!isRealGroup && !root) {
        return groups
    }

    var glueOptions = options
    if (expression.size == 1) {
        val node = expression[0]
        if (node is ParseNode.Sizing) {
            glueOptions = options.havingSize(node.size)
        } else if (node is ParseNode.Styling) {
            glueOptions = options.havingStyle(styleMap.getValue(node.style))
        }
    }

    val dummyPrev = makeSpan(listOf(surrounding.first ?: "leftmost"), emptyList(), options)
    val dummyNext = makeSpan(listOf(surrounding.second ?: "rightmost"), emptyList(), options)

    traverseNonSpaceNodes(groups, { node, prev ->
        val prevType = prev.classes.firstOrNull()
        val type = node.classes.firstOrNull()
        if (prevType == "mbin" && type in binRightCanceller) {
            prev.classes[0] = "mord"
        } else if (type == "mbin" && prevType in binLeftCanceller) {
            node.classes[0] = "mord"
        }
        null
    }, TraversalPrev(dummyPrev), dummyNext, root)

    traverseNonSpaceNodes(groups, { node, prev ->
        val prevType = getTypeOfDomTree(prev)
        val type = getTypeOfDomTree(node)
        val space =
            if (prevType != null && type != null) {
                if (node.hasClass("mtight")) tightSpacings[prevType]?.get(type) else spacings[prevType]?.get(type)
            } else {
                null
            }
        space?.let { makeGlue(it, glueOptions) }
    }, TraversalPrev(dummyPrev), dummyNext, root)

    return groups
}

private class TraversalPrev(
    var node: HtmlDomNode,
    var insertAfter: ((HtmlDomNode) -> Unit)? = null,
)

private class Cursor(var i: Int)

private fun traverseNonSpaceNodes(
    nodes: MutableList<HtmlDomNode>,
    callback: (node: HtmlDomNode, prev: HtmlDomNode) -> HtmlDomNode?,
    prev: TraversalPrev,
    next: HtmlDomNode?,
    isRoot: Boolean,
) {
    if (next != null) {
        nodes.add(next)
    }
    val cursor = Cursor(0)
    while (cursor.i < nodes.size) {
        val node = nodes[cursor.i]
        val partialGroup = checkPartialGroup(node)
        if (partialGroup != null) {
            traverseNonSpaceNodes(partialGroup, callback, prev, null, isRoot)
            cursor.i += 1
            continue
        }

        val nonspace = !node.hasClass("mspace")
        if (nonspace) {
            val result = callback(node, prev.node)
            if (result != null) {
                val insertAfter = prev.insertAfter
                if (insertAfter != null) {
                    insertAfter(result)
                } else {
                    nodes.add(0, result)
                    cursor.i += 1
                }
            }
        }

        if (nonspace) {
            prev.node = node
        } else if (isRoot && node.hasClass("katex-newline")) {
            prev.node = makeSpan(listOf("leftmost"))
        }

        val index = cursor.i
        prev.insertAfter = { n ->
            nodes.add(index + 1, n)
            cursor.i += 1
        }
        cursor.i += 1
    }
    if (next != null) {
        nodes.removeAt(nodes.lastIndex)
    }
}

@Suppress("UNCHECKED_CAST")
private fun checkPartialGroup(node: HtmlDomNode): MutableList<HtmlDomNode>? =
    when {
        node is DocumentFragment -> node.children
        node is Anchor -> node.children as MutableList<HtmlDomNode>
        node is Span && node.hasClass("enclosing") -> node.children as MutableList<HtmlDomNode>
        else -> null
    }

private fun getOutermostNode(node: HtmlDomNode, side: String): HtmlDomNode {
    val children = checkPartialGroup(node)
    if (!children.isNullOrEmpty()) {
        if (side == "right") {
            return getOutermostNode(children.last(), "right")
        } else if (side == "left") {
            return getOutermostNode(children.first(), "left")
        }
    }
    return node
}

/** The math class of [node]'s outermost atom on [side], or its own when [side] is null. */
fun getTypeOfDomTree(node: HtmlDomNode?, side: String? = null): String? {
    if (node == null) {
        return null
    }
    val outer = if (side != null) getOutermostNode(node, side) else node
    return outer.classes.firstOrNull()?.takeIf { it in domTypes }
}

fun makeNullDelimiter(options: Options, classes: List<String>): Span =
    makeSpan(classes + "nulldelimiter" + options.baseSizingClasses())

/** Builds [group] with its registered builder, wrapping it in a sizing span if needed. */
fun buildGroup(group: ParseNode?, options: Options, baseOptions: Options? = null): HtmlDomNode {
    if (group == null) {
        return makeSpan()
    }
    val builder = Functions.htmlGroupBuilders[group.type] ?: throw ParseError("Got group of unknown type: '${group.type}'")
    var groupNode = builder(group, options)
    if (baseOptions != null && options.size != baseOptions.size) {
        groupNode = makeSpan(options.sizingClasses(baseOptions), listOf(groupNode), options)
        val multiplier = options.sizeMultiplier / baseOptions.sizeMultiplier
        groupNode.height *= multiplier
        groupNode.depth *= multiplier
    }
    return groupNode
}

private fun buildHTMLUnbreakable(children: List<HtmlDomNode>, options: Options): Span {
    val body = makeSpan(listOf("katex-base"), children, options)
    val strut = makeSpan(listOf("katex-strut"))
    strut.style.height = makeEm(body.height + body.depth)
    if (body.depth != 0.0) {
        strut.style.verticalAlign = makeEm(-body.depth)
    }
    body.children.add(0, strut)
    return body
}

/** The root of KaTeX's HTML output: unbreakable `katex-base` runs and an optional tag. */
fun buildHTML(tree: List<ParseNode>, options: Options): Span {
    var body = tree
    var tag: List<ParseNode>? = null
    val only = tree.singleOrNull()
    if (only is ParseNode.Tag) {
        tag = only.tag
        body = only.body
    }

    val expression = buildExpression(body, options, isRealGroup = true, root = true)

    var eqnNum: HtmlDomNode? = null
    if (expression.size == 2 && expression[1].hasClass("katex-tag")) {
        eqnNum = expression.removeAt(1)
    }

    val children = mutableListOf<HtmlDomNode>()
    var parts = mutableListOf<HtmlDomNode>()
    var i = 0
    while (i < expression.size) {
        parts.add(expression[i])
        if (expression[i].hasClass("mbin") || expression[i].hasClass("mrel") || expression[i].hasClass("allowbreak")) {
            var nobreak = false
            while (i < expression.size - 1 && expression[i + 1].hasClass("mspace") && !expression[i + 1].hasClass("katex-newline")) {
                i += 1
                parts.add(expression[i])
                if (expression[i].hasClass("nobreak")) {
                    nobreak = true
                }
            }
            if (!nobreak) {
                children.add(buildHTMLUnbreakable(parts, options))
                parts = mutableListOf()
            }
        } else if (expression[i].hasClass("katex-newline")) {
            parts.removeAt(parts.lastIndex)
            if (parts.isNotEmpty()) {
                children.add(buildHTMLUnbreakable(parts, options))
                parts = mutableListOf()
            }
            children.add(expression[i])
        }
        i += 1
    }
    if (parts.isNotEmpty()) {
        children.add(buildHTMLUnbreakable(parts, options))
    }

    var tagChild: Span? = null
    if (tag != null) {
        tagChild = buildHTMLUnbreakable(buildExpression(tag, options, true), options)
        tagChild.classes.clear()
        tagChild.classes.add("katex-tag")
        children.add(tagChild)
    } else if (eqnNum != null) {
        children.add(eqnNum)
    }

    val htmlNode = makeSpan(listOf("katex-html"), children)
    if (tagChild != null) {
        val strut = tagChild.children[0] as Span
        strut.style.height = makeEm(htmlNode.height + htmlNode.depth)
        if (htmlNode.depth != 0.0) {
            strut.style.verticalAlign = makeEm(-htmlNode.depth)
        }
    }
    return htmlNode
}
