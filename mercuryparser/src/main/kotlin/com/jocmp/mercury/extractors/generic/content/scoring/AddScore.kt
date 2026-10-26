package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

fun addScore(
    node: Selection,
    doc: Doc,
    amount: Double,
): Selection {
    try {
        val score = getOrInitScore(node, doc) + amount
        setScore(node, doc, score)
    } catch (_: Throwable) {
        // Ignoring; error occurs in scoreNode
    }
    return node
}
