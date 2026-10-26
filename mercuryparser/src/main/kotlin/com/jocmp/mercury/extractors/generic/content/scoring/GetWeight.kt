package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Selection

// Get the score of a node based on its className and id.
fun getWeight(node: Selection): Int {
    val classes = node.attr("class")
    val id = node.attr("id")
    var score = 0

    if (id != null) {
        // if id exists, try to score on both positive and negative
        if (POSITIVE_SCORE_RE.containsMatchIn(id)) {
            score += 25
        }
        if (NEGATIVE_SCORE_RE.containsMatchIn(id)) {
            score -= 25
        }
    }

    if (classes != null) {
        if (score == 0) {
            // if classes exist and id did not contribute to score
            // try to score on both positive and negative
            if (POSITIVE_SCORE_RE.containsMatchIn(classes)) {
                score += 25
            }
            if (NEGATIVE_SCORE_RE.containsMatchIn(classes)) {
                score -= 25
            }
        }

        // even if score has been set by id, add score for
        // possible photo matches
        // "try to keep photos if we can"
        if (PHOTO_HINTS_RE.containsMatchIn(classes)) {
            score += 10
        }

        // add 25 if class matches entry-content-asset,
        // a class apparently instructed for use in the
        // Readability publisher guidelines
        // https://www.readability.com/developers/guidelines
        if (READABILITY_ASSET.containsMatchIn(classes)) {
            score += 25
        }
    }

    return score
}
