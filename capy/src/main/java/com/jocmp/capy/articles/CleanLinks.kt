package com.jocmp.capy.articles

import org.jsoup.nodes.Element

internal fun cleanLinks(element: Element) {
    element.getElementsByTag("img").forEach { child ->
        child.attr("src", child.absUrl("src"))
    }

    element.select("img[data-src]").forEach { child ->
        child.attr("src", child.absUrl("data-src"))
    }
}
