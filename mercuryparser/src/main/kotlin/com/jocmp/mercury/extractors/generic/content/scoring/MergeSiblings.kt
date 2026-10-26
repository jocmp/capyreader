package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.utils.dom.linkDensity
import com.jocmp.mercury.utils.dom.textLength
import com.jocmp.mercury.utils.text.hasSentenceEnd
import kotlin.math.max

// Now that we have a top_candidate, look through the siblings of
// it to see if any of them are decently scored. If they are, they
// may be split parts of the content (Like two divs, a preamble and
// a body.) Example:
// http://articles.latimes.com/2009/oct/14/business/fi-bigtvs14
fun mergeSiblings(
    candidate: Selection,
    topScore: Double,
    doc: Doc,
): Selection {
    val candidateEl = candidate.elements.firstOrNull() ?: return candidate
    if (candidate.parent().length == 0) {
        return candidate
    }

    val siblingScoreThreshold = max(10.0, topScore * 0.25)
    val wrappingDiv = doc.document.createElement("div")

    candidate.parent().children().each { _, sibling ->
        // Ignore tags like BR, HR, etc
        if (NON_TOP_CANDIDATE_TAGS_RE.containsMatchIn(sibling.tagName())) {
            return@each
        }

        val siblingSel = doc.wrap(sibling)
        val siblingScore = getScore(siblingSel) ?: return@each

        if (sibling === candidateEl) {
            wrappingDiv.appendChild(sibling)
        } else {
            var contentBonus = 0.0
            val density = linkDensity(siblingSel)

            // If sibling has a very low link density,
            // give it a small bonus
            if (density < 0.05) {
                contentBonus += 20
            }

            // If sibling has a high link density,
            // give it a penalty
            if (density >= 0.5) {
                contentBonus -= 20
            }

            // If sibling node has the same class as
            // candidate, give it a bonus
            if (siblingSel.attr("class") == candidate.attr("class")) {
                contentBonus += topScore * 0.2
            }

            val newScore = siblingScore + contentBonus

            if (newScore >= siblingScoreThreshold) {
                wrappingDiv.appendChild(sibling)
            } else if (sibling.tagName() == "p") {
                val siblingContent = siblingSel.text()
                val siblingContentLength = textLength(siblingContent)

                if (siblingContentLength > 80 && density < 0.25) {
                    wrappingDiv.appendChild(sibling)
                } else if (siblingContentLength <= 80 && density == 0.0 && hasSentenceEnd(siblingContent)) {
                    wrappingDiv.appendChild(sibling)
                }
            }
        }
    }

    if (wrappingDiv.children().size == 1 && wrappingDiv.child(0) === candidateEl) {
        return candidate
    }

    return doc.wrap(wrappingDiv)
}
