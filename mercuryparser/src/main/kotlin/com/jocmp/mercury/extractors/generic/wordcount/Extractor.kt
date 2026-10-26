package com.jocmp.mercury.extractors.generic.wordcount

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.text.normalizeSpaces

private fun getWordCount(content: String): Int {
    val doc = Doc.load(content, isDocument = false)
    val first = doc("div").first()
    val text = normalizeSpaces(first.text())
    return text.split(Regex("""\s""")).size
}

private fun getWordCountAlt(content: String): Int {
    var c = content
    c = c.replace(Regex("""<[^>]*>"""), " ")
    c = c.replace(Regex("""\s+"""), " ")
    c = c.trim()
    return c.split(" ").size
}

fun extractGenericWordCount(content: String): Int {
    var count = getWordCount(content)
    if (count == 1) count = getWordCountAlt(content)
    return count
}
