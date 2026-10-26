package com.jocmp.mercury.extractors.generic.content.scoring

import com.jocmp.mercury.dsl.Selection

// Score an individual node. Has some smarts for paragraphs, otherwise
// just scores based on tag.
fun scoreNode(node: Selection): Double {
    val tagName = node.elements.firstOrNull()?.tagName() ?: return 0.0

    // TODO: Consider ordering by most likely.
    // E.g., if divs are a more common tag on a page,
    // Could save doing that regex test on every node – AP
    if (PARAGRAPH_SCORE_TAGS.containsMatchIn(tagName)) {
        return scoreParagraph(node)
    }
    if (tagName.lowercase() == "div") {
        return 5.0
    }
    if (CHILD_CONTENT_TAGS.containsMatchIn(tagName)) {
        return 3.0
    }
    if (BAD_TAGS.containsMatchIn(tagName)) {
        return -3.0
    }
    if (tagName.lowercase() == "th") {
        return -5.0
    }
    return 0.0
}
