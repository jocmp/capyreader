package com.jocmp.mercury.extractors.generic.excerpt

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.extractFromMeta
import com.jocmp.mercury.utils.dom.stripTags
import com.jocmp.mercury.utils.ellipsize

fun clean(
    content: String,
    @Suppress("UNUSED_PARAMETER") doc: Doc,
    maxLength: Int = 200,
): String {
    val collapsed = content.replace(Regex("""[\s\n]+"""), " ").trim()
    return ellipsize(collapsed, maxLength, ellipse = "&hellip;")
}

fun extractGenericExcerpt(
    doc: Doc,
    content: String?,
    metaCache: List<String>,
): String? {
    val excerpt = extractFromMeta(doc, EXCERPT_META_SELECTORS, metaCache)
    if (excerpt != null) {
        return clean(stripTags(excerpt, doc), doc)
    }
    if (content == null) return null
    // Fall back to excerpting from the extracted content
    val maxLength = 200
    val shortContent = content.take(maxLength * 5)
    val tmp = Doc.load(shortContent, isDocument = false)
    return clean(tmp.root().text(), doc, maxLength)
}
