package com.jocmp.mercury.extractors.generic.title

import com.jocmp.mercury.cleaners.cleanTitle
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta
import com.jocmp.mercury.utils.dom.extractFromSelectors

fun extractGenericTitle(
    doc: Doc,
    url: String,
    metaCache: List<String>,
): String {
    // First, check to see if we have a matching meta tag that we can make
    // use of that is strongly associated with the headline.
    var title: String?

    title = extractFromMeta(doc, STRONG_TITLE_META_TAGS, metaCache)
    if (title != null) return cleanTitle(title, doc, url)

    // Second, look through our content selectors for the most likely
    // article title that is strongly associated with the headline.
    title = extractFromSelectors(doc, STRONG_TITLE_SELECTORS)
    if (title != null) return cleanTitle(title, doc, url)

    // Third, check for weaker meta tags that may match.
    title = extractFromMeta(doc, WEAK_TITLE_META_TAGS, metaCache)
    if (title != null) return cleanTitle(title, doc, url)

    // Last, look for weaker selector tags that may match.
    title = extractFromSelectors(doc, WEAK_TITLE_SELECTORS)
    if (title != null) return cleanTitle(title, doc, url)

    // If no matches, return an empty string
    return ""
}
