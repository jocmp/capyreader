package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import java.net.URI

fun markToKeep(
    article: Selection,
    doc: Doc,
    url: String?,
    tags: List<String> = emptyList(),
): Doc {
    var effectiveTags = if (tags.isEmpty()) KEEP_SELECTORS else tags

    if (url != null) {
        val parsed =
            try {
                URI.create(url)
            } catch (_: Throwable) {
                null
            }
        if (parsed != null && parsed.scheme != null && parsed.host != null) {
            effectiveTags = effectiveTags + "iframe[src^=\"${parsed.scheme}://${parsed.host}\"]"
        }
    }

    article.find(effectiveTags.joinToString(",")).each { _, node ->
        node.addClass(KEEP_CLASS)
    }
    return doc
}
