package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

// Given a node type to search for, and a list of meta tag names to
// search for, find a meta tag associated.
fun extractFromMeta(
    doc: Doc,
    metaNames: List<String>,
    cachedNames: List<String>,
    cleanTags: Boolean = true,
): String? {
    val foundNames = metaNames.filter { cachedNames.contains(it) }

    for (name in foundNames) {
        val type = "name"
        val value = "value"

        val nodes = doc("meta[$type=\"$name\"]")

        // Get the unique value of every matching node, in case there
        // are two meta tags with the same name and value.
        // Remove empty values.
        val values =
            (0 until nodes.length)
                .mapNotNull { nodes.get(it)?.attr(value) }
                .filter { it.isNotEmpty() }
                .distinct()

        // If we have more than one value for the same name, we have a
        // conflict and can't trust any of them. Skip this name. If we have
        // zero, that means our meta tags had no values. Skip this name
        // also.
        if (values.size == 1) {
            val metaValue =
                if (cleanTags) {
                    stripTags(values[0], doc)
                } else {
                    values[0]
                }
            return metaValue
        }
    }

    // If nothing is found, return null
    return null
}
