package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

// strips all tags from a string of text
fun stripTags(
    text: String,
    @Suppress("UNUSED_PARAMETER") doc: Doc,
): String {
    // Wrapping text in html element prevents errors when text
    // has no html
    val tmp = Doc.load("<span>$text</span>", isDocument = false)
    val cleanText = tmp("span").text()
    return cleanText.ifEmpty { text }
}
