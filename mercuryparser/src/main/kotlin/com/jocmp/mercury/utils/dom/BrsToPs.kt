package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc

// ## NOTES:
// Another good candidate for refactoring/optimizing.
// Very imperative code, I don't love it. - AP

//  Given cheerio object, convert consecutive <br /> tags into
//  <p /> tags instead.
//
//  :param $: A cheerio object
fun brsToPs(doc: Doc): Doc {
    var collapsing = false
    doc("br").each { _, element ->
        val nextElement = element.nextElementSibling()

        if (nextElement != null && nextElement.tagName().lowercase() == "br") {
            collapsing = true
            element.remove()
        } else if (collapsing) {
            collapsing = false
            paragraphize(element, doc, true)
        }
    }
    return doc
}
