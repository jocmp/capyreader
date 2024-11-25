package com.jocmp.capy.articles

import org.jsoup.nodes.Document

fun removeImages(document: Document) {
    document.select("img").forEach {
        it.remove()
    }
}
