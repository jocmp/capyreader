package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.generic.content.scoring.getWeight
import com.jocmp.mercury.utils.text.normalizeSpaces

fun cleanHeaders(
    article: Selection,
    doc: Doc,
    title: String = "",
): Doc {
    article.find(HEADER_TAG_LIST).each { _, header ->
        val sel = doc.wrap(header)

        if (header.hasClass(KEEP_CLASS)) {
            return@each
        }

        // Remove any headers that appear before all other p tags in the
        // document. This probably means that it was part of the title, a
        // subtitle or something else extraneous like a datestamp or byline,
        // all of which should be handled by other metadata handling.
        val hasPriorP =
            generateSequence(header.previousElementSibling()) { it.previousElementSibling() }
                .any { it.tagName() == "p" }
        if (!hasPriorP) {
            header.remove()
            return@each
        }

        // Remove any headers that match the title exactly.
        if (normalizeSpaces(header.text()) == title) {
            header.remove()
            return@each
        }

        // If this header has a negative weight, it's probably junk.
        // Get rid of it.
        if (getWeight(sel) < 0) {
            header.remove()
        }
    }
    return doc
}
