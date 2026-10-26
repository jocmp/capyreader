package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

fun setScore(
    node: Selection,
    @Suppress("UNUSED_PARAMETER") doc: Doc,
    score: Double,
): Selection {
    node.attr("score", score.toString())
    return node
}
