package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

fun stripJunkTags(
    article: Selection,
    doc: Doc,
    tags: List<String> = emptyList(),
): Doc {
    val effectiveTags = if (tags.isEmpty()) STRIP_OUTPUT_TAGS else tags

    // Remove matching elements, but ignore
    // any element with a class of mercury-parser-keep
    article.find(effectiveTags.joinToString(",")).each { _, node ->
        if (!node.hasClass(KEEP_CLASS)) {
            node.remove()
        }
    }
    return doc
}
