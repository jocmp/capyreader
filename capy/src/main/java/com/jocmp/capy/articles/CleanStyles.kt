package com.jocmp.capy.articles

import org.jsoup.nodes.Document

fun cleanStyles(document: Document) {
   document.select("#article-body-content *").forEach {
        it.removeAttr("style")
    }
}
