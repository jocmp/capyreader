package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

private fun convertDivs(doc: Doc): Doc {
    doc("div").each { _, div ->
        val convertible = div.select(DIV_TO_P_BLOCK_TAGS).size == 0
        if (convertible) {
            convertNodeTo(doc.wrap(div), doc, "p")
        }
    }
    return doc
}

private fun convertSpans(doc: Doc): Doc {
    doc("span").each { _, span ->
        val convertible = span.parents().none { it.tagName() in setOf("p", "div", "li", "figcaption") }
        if (convertible) {
            convertNodeTo(doc.wrap(span), doc, "p")
        }
    }
    return doc
}

// Loop through the provided doc, and convert any p-like elements to
// actual paragraph tags.
//
//   Things fitting this criteria:
//   * Multiple consecutive <br /> tags.
//   * <div /> tags without block level elements inside of them
//   * <span /> tags who are not children of <p /> or <div /> tags.
//
//   :param $: A cheerio object to search
//   :return cheerio object with new p elements
//   (By-reference mutation, though. Returned just for convenience.)
fun convertToParagraphs(doc: Doc): Doc {
    var d = doc
    d = brsToPs(d)
    d = convertDivs(d)
    d = convertSpans(d)
    return d
}
