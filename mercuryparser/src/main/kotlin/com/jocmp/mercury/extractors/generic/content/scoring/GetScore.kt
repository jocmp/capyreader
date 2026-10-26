package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Selection

// returns the score of a node based on
// the node's score attribute
// returns null if no score set
fun getScore(node: Selection): Double? {
    val raw = node.attr("score") ?: return null
    return raw.toDoubleOrNull()
}
