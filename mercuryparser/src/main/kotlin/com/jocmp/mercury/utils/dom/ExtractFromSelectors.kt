package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

private fun isGoodNode(
    node: Selection,
    maxChildren: Int,
): Boolean {
    // If it has a number of children, it's more likely a container
    // element. Skip it.
    if (node.children().length > maxChildren) {
        return false
    }
    // If it looks to be within a comment, skip it.
    if (withinComment(node)) {
        return false
    }
    return true
}

// Given a a list of selectors find content that may
// be extractable from the document. This is for flat
// meta-information, like author, title, date published, etc.
fun extractFromSelectors(
    doc: Doc,
    selectors: List<String>,
    maxChildren: Int = 1,
    textOnly: Boolean = true,
): String? {
    for (selector in selectors) {
        val nodes = doc(selector)

        // If we didn't get exactly one of this selector, this may be
        // a list of articles or comments. Skip it.
        if (nodes.length == 1) {
            val node = nodes.first()
            if (isGoodNode(node, maxChildren)) {
                val content = if (textOnly) node.text() else node.html()
                if (content.isNotEmpty()) {
                    return content
                }
            }
        }
    }
    return null
}
