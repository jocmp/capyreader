package com.jocmp.mercury.cleaners

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.stripTags
import com.jocmp.mercury.utils.text.normalizeSpaces

fun cleanTitle(
    title: String,
    doc: Doc,
    url: String = "",
): String {
    var t = title
    // If title has |, :, or - in it, see if
    // we can clean it up.
    if (TITLE_SPLITTERS_RE.containsMatchIn(t)) {
        t = resolveSplitTitle(t, url)
    }

    // Final sanity check that we didn't get a crazy title.
    // if (title.length > 150 || title.length < 15) {
    if (t.length > 150) {
        // If we did, return h1 from the document if it exists
        val h1 = doc("h1")
        if (h1.length == 1) {
            t = h1.text()
        }
    }

    // strip any html tags in the title text
    return normalizeSpaces(stripTags(t, doc).trim())
}
