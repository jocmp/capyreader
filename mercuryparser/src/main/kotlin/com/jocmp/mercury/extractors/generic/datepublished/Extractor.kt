package com.jocmp.mercury.extractors.generic.datepublished

import com.jocmp.mercury.cleaners.cleanDatePublished
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta
import com.jocmp.mercury.utils.dom.extractFromSelectors
import com.jocmp.mercury.utils.text.extractFromUrl

fun extractGenericDatePublished(
    doc: Doc,
    url: String,
    metaCache: List<String>,
): String? {
    // First, check to see if we have a matching meta tag
    // that we can make use of.
    // Don't try cleaning tags from this string
    var datePublished = extractFromMeta(doc, DATE_PUBLISHED_META_TAGS, metaCache, cleanTags = false)
    if (datePublished != null) return cleanDatePublished(datePublished)

    // Second, look through our selectors looking for potential
    // date_published's.
    datePublished = extractFromSelectors(doc, DATE_PUBLISHED_SELECTORS)
    if (datePublished != null) return cleanDatePublished(datePublished)

    // Lastly, look to see if a dately string exists in the URL
    datePublished = extractFromUrl(url, DATE_PUBLISHED_URL_RES)
    if (datePublished != null) return cleanDatePublished(datePublished)

    return null
}
