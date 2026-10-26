package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

private fun removeAllButWhitelist(
    article: Selection,
    doc: Doc,
): Selection {
    article.find("*").each { _, node ->
        val attrs = getAttrs(node)
        val filtered = attrs.filterKeys { WHITELIST_ATTRS_RE.containsMatchIn(it) }
        setAttrs(node, filtered)
    }

    // Remove the mercury-parser-keep class from result
    article.find(".$KEEP_CLASS").elements.forEach { it.removeClass(KEEP_CLASS) }

    return article
}

// Remove attributes like style or align
fun cleanAttributes(
    article: Selection,
    doc: Doc,
): Selection {
    // Grabbing the parent because at this point
    // $article will be wrapped in a div which will
    // have a score set on it.
    val parent = article.parent()
    val parentTag = parent.elements.firstOrNull()?.tagName()?.lowercase()
    val parentIsSyntheticWrapper = parent.length > 0 && (parentTag == "body" || parentTag == "html")
    val target = if (parent.length > 0 && !parentIsSyntheticWrapper) parent else article
    return removeAllButWhitelist(target, doc)
}
