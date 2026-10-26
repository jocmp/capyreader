package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

fun stripUnlikelyCandidates(doc: Doc): Doc {
    //  Loop through the provided document and remove any non-link nodes
    //  that are unlikely candidates for article content.
    //
    //  Links are ignored because there are very often links to content
    //  that are identified as non-body-content, but may be inside
    //  article-like content.
    //
    //  :param $: a cheerio object to strip nodes from
    //  :return $: the cleaned cheerio object
    doc("*").each { _, node ->
        if (node.tagName().lowercase() == "a") return@each
        val classes = node.attr("class").ifEmpty { null }
        val id = node.attr("id").ifEmpty { null }
        if (id == null && classes == null) return@each

        val classAndId = "${classes ?: ""} ${id ?: ""}"
        if (CANDIDATES_WHITELIST.containsMatchIn(classAndId)) {
            return@each
        }
        if (CANDIDATES_BLACKLIST.containsMatchIn(classAndId)) {
            node.remove()
        }
    }
    return doc
}
