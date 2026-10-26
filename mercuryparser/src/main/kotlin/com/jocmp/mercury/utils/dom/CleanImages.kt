package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

private fun cleanForHeight(
    img: Selection,
    doc: Doc,
): Doc {
    val height = img.attr("height")?.toIntOrNull() ?: 0
    val width = img.attr("width")?.toIntOrNull() ?: 20

    // Remove images that explicitly have very small heights or
    // widths, because they are most likely shims or icons,
    // which aren't very useful for reading.
    if ((if (height > 0) height else 20) < 10 || width < 10) {
        img.remove()
    } else if (height > 0) {
        // Don't ever specify a height on images, so that we can
        // scale with respect to width without screwing up the
        // aspect ratio.
        img.removeAttr("height")
    }
    return doc
}

// Cleans out images where the source string matches transparent/spacer/etc
// TODO This seems very aggressive - AP
private fun removeSpacers(
    img: Selection,
    doc: Doc,
): Doc {
    val src = img.attr("src") ?: return doc
    if (SPACER_RE.containsMatchIn(src)) {
        img.remove()
    }
    return doc
}

fun cleanImages(
    article: Selection,
    doc: Doc,
): Doc {
    article.find("img").each { _, raw ->
        val img = doc.wrap(raw)
        cleanForHeight(img, doc)
        removeSpacers(img, doc)
    }
    return doc
}
