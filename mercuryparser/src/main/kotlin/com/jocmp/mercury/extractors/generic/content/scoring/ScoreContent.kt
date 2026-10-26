package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.utils.dom.convertNodeTo

private fun convertSpans(
    node: Selection,
    doc: Doc,
) {
    val el = node.elements.firstOrNull() ?: return
    val tagName = el.tagName()
    if (tagName == "span") {
        // convert spans to divs
        convertNodeTo(node, doc, "div")
    }
}

private fun addScoreTo(
    node: Selection?,
    doc: Doc,
    score: Double,
) {
    if (node != null && node.length > 0) {
        convertSpans(node, doc)
        addScore(node, doc, score)
    }
}

private fun scorePs(
    doc: Doc,
    weightNodes: Boolean,
): Doc {
    doc("p, pre").each { _, node ->
        if (node.hasAttr("score")) return@each
        var selection = doc.wrap(node)
        // The raw score for this paragraph, before we add any parent/child
        // scores.
        selection = setScore(selection, doc, getOrInitScore(selection, doc, weightNodes))

        val parent = selection.parent()
        val rawScore = scoreNode(selection)

        addScoreTo(parent, doc, rawScore)
        if (parent.length > 0) {
            // Add half of the individual content score to the
            // grandparent
            addScoreTo(parent.parent(), doc, rawScore / 2)
        }
    }
    return doc
}

// score content. Parents get the full value of their children's
// content score, grandparents half
fun scoreContent(
    doc: Doc,
    weightNodes: Boolean = true,
): Doc {
    // First, look for special hNews based selectors and give them a big
    // boost, if they exist
    HNEWS_CONTENT_SELECTORS.forEach { (parentSelector, childSelector) ->
        doc("$parentSelector $childSelector").each { _, node ->
            val parents = node.parents().filter { it.`is`(parentSelector) }
            if (parents.isNotEmpty()) {
                addScore(doc.wrap(parents.first()), doc, 80.0)
            }
        }
    }

    // Doubling this again
    // Previous solution caused a bug
    // in which parents weren't retaining
    // scores. This is not ideal, and
    // should be fixed.
    scorePs(doc, weightNodes)
    scorePs(doc, weightNodes)
    return doc
}
