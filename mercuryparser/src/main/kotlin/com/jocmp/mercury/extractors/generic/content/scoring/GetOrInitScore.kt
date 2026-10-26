package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// gets and returns the score if it exists
// if not, initializes a score based on
// the node's tag type
fun getOrInitScore(
    node: Selection,
    doc: Doc,
    weightNodes: Boolean = true,
): Double {
    val existing = getScore(node)
    if (existing != null) {
        return existing
    }

    var score = scoreNode(node)

    if (weightNodes) {
        score += getWeight(node).toDouble()
    }

    addToParent(node, doc, score)

    return score
}
