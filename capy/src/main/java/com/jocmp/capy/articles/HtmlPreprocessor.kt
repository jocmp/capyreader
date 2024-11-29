package com.jocmp.capy.articles

import org.jsoup.Jsoup

object HtmlPreprocessor {
    fun clean(html: String, siteURL: String?, hideImages: Boolean): String {
        val document = Jsoup.parse(html).apply {
            siteURL?.let { setBaseUri(it) }
        }

        cleanStyles(document)
        cleanLinks(document)
        if (hideImages) {
            removeImages(document)
        }
        wrapTables(document)

        return document.html()
    }
}
