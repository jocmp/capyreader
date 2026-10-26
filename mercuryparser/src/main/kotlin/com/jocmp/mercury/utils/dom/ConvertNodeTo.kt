package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

fun convertNodeTo(
    node: Selection,
    doc: Doc,
    tag: String = "p",
): Doc {
    val raw = node.elements.firstOrNull() ?: return doc
    // Jsoup's tagName() rewrites the tag in place, preserving attributes
    // and child nodes. This matches cheerio's $node.replaceWith(`<${tag} ...>${html}</${tag}>`)
    // semantics without round-tripping through a string parse.
    raw.tagName(tag)
    return doc
}
