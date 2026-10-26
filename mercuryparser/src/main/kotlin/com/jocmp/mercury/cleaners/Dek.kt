package com.jocmp.mercury.cleaners

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.stripTags
import com.jocmp.mercury.utils.text.excerptContent
import com.jocmp.mercury.utils.text.normalizeSpaces

// Take a dek HTML fragment, and return the cleaned version of it.
// Return None if the dek wasn't good enough.
fun cleanDek(
    dek: String,
    doc: Doc,
    excerpt: String? = null,
): String? {
    // Sanity check that we didn't get too short or long of a dek.
    if (dek.length > 1000 || dek.length < 5) return null

    // Check that dek isn't the same as excerpt
    if (excerpt != null && excerptContent(excerpt, 10) == excerptContent(dek, 10)) {
        return null
    }

    val dekText = stripTags(dek, doc)

    // Plain text links shouldn't exist in the dek. If we have some, it's
    // not a good dek - bail.
    if (TEXT_LINK_RE.containsMatchIn(dekText)) return null

    return normalizeSpaces(dekText.trim())
}
