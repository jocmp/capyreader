package com.jocmp.mercury.extractors.generic.author

import com.jocmp.mercury.cleaners.cleanAuthor
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta
import com.jocmp.mercury.utils.dom.extractFromSelectors

fun extractGenericAuthor(
    doc: Doc,
    metaCache: List<String>,
): String? {
    var author: String?

    // First, check to see if we have a matching
    // meta tag that we can make use of.
    author = extractFromMeta(doc, AUTHOR_META_TAGS, metaCache)
    if (author != null && author.length < AUTHOR_MAX_LENGTH) {
        return cleanAuthor(author)
    }

    // Second, look through our selectors looking for potential authors.
    author = extractFromSelectors(doc, AUTHOR_SELECTORS, 2)
    if (author != null && author.length < AUTHOR_MAX_LENGTH) {
        return cleanAuthor(author)
    }

    // Last, use our looser regular-expression based selectors for
    // potential authors.
    for ((selector, regex) in BYLINE_SELECTORS_RE) {
        val node = doc(selector)
        if (node.length == 1) {
            val text = node.text()
            if (regex.containsMatchIn(text)) {
                return cleanAuthor(text)
            }
        }
    }

    return null
}
