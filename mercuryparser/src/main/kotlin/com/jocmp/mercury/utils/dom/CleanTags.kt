package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.generic.content.scoring.getOrInitScore
import com.jocmp.mercury.extractors.generic.content.scoring.getScore
import com.jocmp.mercury.extractors.generic.content.scoring.scoreCommas
import com.jocmp.mercury.extractors.generic.content.scoring.setScore
import com.jocmp.mercury.utils.text.normalizeSpaces

private fun removeUnlessContent(
    node: Selection,
    doc: Doc,
    weight: Double,
) {
    // Explicitly save entry-content-asset tags, which are
    // noted as valuable in the Publisher guidelines. For now
    // this works everywhere. We may want to consider making
    // this less of a sure-thing later.
    if (node.elements.firstOrNull()?.hasClass("entry-content-asset") == true) {
        return
    }

    val content = normalizeSpaces(node.text())

    if (scoreCommas(content) < 10) {
        val pCount = node.find("p").length
        val inputCount = node.find("input").length

        // Looks like a form, too many inputs.
        if (inputCount > pCount / 3) {
            node.remove()
            return
        }

        val contentLength = content.length
        val imgCount = node.find("img").length

        // Content is too short, and there are no images, so
        // this is probably junk content.
        if (contentLength < 25 && imgCount == 0) {
            node.remove()
            return
        }

        val density = linkDensity(node)

        // Too high of link density, is probably a menu or
        // something similar.
        if (weight < 25 && density > 0.2 && contentLength > 75) {
            node.remove()
            return
        }

        // Too high of a link density, despite the score being
        // high.
        if (weight >= 25 && density > 0.5) {
            // Don't remove the node if it's a list and the
            // previous sibling starts with a colon though. That
            // means it's probably content.
            val tagName = node.elements.firstOrNull()?.tagName()?.lowercase()
            val nodeIsList = tagName == "ol" || tagName == "ul"
            if (nodeIsList) {
                val prev = node.elements.firstOrNull()?.previousElementSibling()
                if (prev != null && normalizeSpaces(prev.text()).takeLast(1) == ":") {
                    return
                }
            }
            node.remove()
            return
        }

        val scriptCount = node.find("script").length

        // Too many script tags, not enough content.
        if (scriptCount > 0 && contentLength < 150) {
            node.remove()
        }
    }
}

// Given an article, clean it of some superfluous content specified by
// tags. Things like forms, ads, etc.
//
// Tags is an array of tag name's to search through. (like div, form,
// etc)
//
// Return this same doc.
fun cleanTags(
    article: Selection,
    doc: Doc,
): Doc {
    article.find(CLEAN_CONDITIONALLY_TAGS).each { _, node ->
        val sel = doc.wrap(node)
        // If marked to keep, skip it
        if (node.hasClass(KEEP_CLASS) || sel.find(".$KEEP_CLASS").length > 0) {
            return@each
        }

        var weight = getScore(sel)
        if (weight == null) {
            weight = getOrInitScore(sel, doc)
            setScore(sel, doc, weight)
        }

        // drop node if its weight is < 0
        if (weight < 0) {
            sel.remove()
        } else {
            // determine if node seems like content
            removeUnlessContent(sel, doc, weight)
        }
    }
    return doc
}
