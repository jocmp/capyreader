package com.jocmp.capy.articles

import org.jsoup.nodes.Document

object HtmlPostProcessor {
    fun clean(document: Document, hideImages: Boolean) {
        cleanStyles(document)
        if (hideImages) {
            removeImages(document)
        }
        cleanLinks(document)
        wrapTables(document)
    }
}
