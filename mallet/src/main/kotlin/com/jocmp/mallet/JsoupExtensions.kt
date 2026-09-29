package com.jocmp.mallet

import org.jsoup.nodes.Element

internal fun Element.attrInHierarchy(attr: String): String {
    var current: Element? = this

    while (current != null) {
        val value = current.attr(attr)
        if (value.isNotEmpty()) {
            return value
        }
        current = current.parent()
    }

    return ""
}

internal fun Element.ancestors(predicate: (Element) -> Boolean): Sequence<Element> = ancestors().filter(predicate)

private fun Element.ancestors(): Sequence<Element> =
    sequence {
        var current: Element? = this@ancestors.parent()

        while (current != null) {
            yield(current)
            current = current.parent()
        }
    }

internal fun stripHtml(html: String): String {
    val result = StringBuilder()
    var skipping = false

    for (char in html) {
        if (skipping) {
            if (char == '>') {
                skipping = false
            }
        } else if (char == '<') {
            skipping = true
        } else {
            result.append(char)
        }
    }

    return result.toString()
}
