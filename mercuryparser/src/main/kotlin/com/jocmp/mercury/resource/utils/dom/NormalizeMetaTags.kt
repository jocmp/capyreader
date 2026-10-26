package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc

private fun convertMetaProp(
    doc: Doc,
    from: String,
    to: String,
): Doc {
    doc("meta[$from]").each { _, node ->
        val value = node.attr(from)
        node.attr(to, value)
        node.removeAttr(from)
    }
    return doc
}

// For ease of use in extracting from meta tags,
// replace the "content" attribute on meta tags with the
// "value" attribute.
//
// In addition, normalize 'property' attributes to 'name' for ease of
// querying later. See, e.g., og or twitter meta tags.
fun normalizeMetaTags(doc: Doc): Doc {
    convertMetaProp(doc, "content", "value")
    convertMetaProp(doc, "property", "name")
    return doc
}
