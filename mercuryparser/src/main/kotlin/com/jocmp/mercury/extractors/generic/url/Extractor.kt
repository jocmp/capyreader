package com.jocmp.mercury.extractors.generic.url

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta
import java.net.URI

private fun parseDomain(url: String): String? =
    try {
        URI.create(url).host
    } catch (_: Throwable) {
        null
    }

private fun result(url: String): UrlAndDomain = UrlAndDomain(url, parseDomain(url))

fun extractGenericUrl(
    doc: Doc,
    url: String,
    metaCache: List<String>,
): UrlAndDomain {
    val canonical = doc("link[rel=canonical]")
    if (canonical.length != 0) {
        val href = canonical.attr("href")
        if (href != null) {
            return result(href)
        }
    }

    val metaUrl = extractFromMeta(doc, CANONICAL_META_SELECTORS, metaCache)
    if (metaUrl != null) {
        return result(metaUrl)
    }

    return result(url)
}
