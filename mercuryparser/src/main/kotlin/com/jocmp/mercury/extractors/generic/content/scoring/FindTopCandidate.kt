package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// After we've calculated scores, loop through all of the possible
// candidate nodes we found and find the one with the highest score.
fun findTopCandidate(doc: Doc): Selection {
    var candidate: Selection? = null
    var topScore = 0.0

    doc("[score]").each { _, node ->
        // Ignore tags like BR, HR, etc
        if (NON_TOP_CANDIDATE_TAGS_RE.containsMatchIn(node.tagName())) {
            return@each
        }

        val selection = doc.wrap(node)
        val score = getScore(selection) ?: return@each

        if (score > topScore) {
            topScore = score
            candidate = selection
        }
    }

    // If we don't have a candidate, return the body
    // or whatever the first element is
    val resolved = candidate ?: return doc("body").takeIf { it.length > 0 } ?: doc("*").first()

    return mergeSiblings(resolved, topScore, doc)
}
