package com.jocmp.capy.articles

import org.jsoup.nodes.Document

object HtmlPostProcessor {
    fun clean(document: Document, hideImages: Boolean) {
        cleanStyles(document)
        cleanLinks(document)
        if (hideImages) {
            removeImages(document)
        }
        wrapTables(document)
    }
}
