package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

// Given a node, turn it into a P if it is not already a P, and
// make sure it conforms to the constraints of a P tag (I.E. does
// not contain any other block tags.)
//
// If the node is a <br />, it treats the following inline siblings
// as if they were its children.
//
// :param node: The node to paragraphize; this is a raw node
// :param $: The cheerio object to handle dom manipulation
// :param br: Whether or not the passed node is a br
fun paragraphize(
    node: Node,
    doc: Doc,
    br: Boolean = false,
): Doc {
    if (!br) return doc

    var sibling: Node? = node.nextSibling()
    val p = doc.document.createElement("p")

    // while the next node is text or not a block level element
    while (sibling != null &&
        !(sibling is Element && BLOCK_LEVEL_TAGS_RE.containsMatchIn(sibling.tagName()))
    ) {
        val next = sibling.nextSibling()
        p.appendChild(sibling)
        sibling = next
    }

    node.replaceWith(p)
    return doc
}
