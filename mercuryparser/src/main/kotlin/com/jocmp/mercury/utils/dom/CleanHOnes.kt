package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// H1 tags are typically the article title, which should be extracted
// by the title extractor instead. If there's less than 3 of them (<3),
// strip them. Otherwise, turn 'em into H2s.
fun cleanHOnes(
    article: Selection,
    doc: Doc,
): Doc {
    val hOnes = article.find("h1")

    if (hOnes.length < 3) {
        hOnes.each { _, node -> node.remove() }
    } else {
        hOnes.each { _, node -> convertNodeTo(doc.wrap(node), doc, "h2") }
    }
    return doc
}
