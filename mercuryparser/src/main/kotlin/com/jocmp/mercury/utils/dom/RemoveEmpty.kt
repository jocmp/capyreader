package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection

fun removeEmpty(
    article: Selection,
    doc: Doc,
): Doc {
    article.find("p").each { _, p ->
        if (p.select("iframe, img").size == 0 && p.text().trim().isEmpty()) {
            p.remove()
        }
    }
    return doc
}
