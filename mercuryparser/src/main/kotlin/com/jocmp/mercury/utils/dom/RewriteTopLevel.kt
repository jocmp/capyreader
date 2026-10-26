package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

// Rewrite the tag name to div if it's a top level node like body or
// html to avoid later complications with multiple body tags.
fun rewriteTopLevel(
    @Suppress("UNUSED_PARAMETER") article: Selection,
    doc: Doc,
): Doc {
    // I'm not using context here because
    // it's problematic when converting the
    // top-level/root node - AP
    var d = doc
    d = convertNodeTo(d("html"), d, "div")
    d = convertNodeTo(d("body"), d, "div")
    return d
}
