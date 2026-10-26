package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// Adds 1/4 of a child's score to its parent
fun addToParent(
    node: Selection,
    doc: Doc,
    score: Double,
): Selection {
    val parent = node.parent()
    if (parent.length > 0) {
        addScore(parent, doc, score * 0.25)
    }
    return node
}
