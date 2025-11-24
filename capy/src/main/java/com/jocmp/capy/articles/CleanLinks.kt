package com.jocmp.capy.articles

import org.jsoup.nodes.Element

internal fun cleanLinks(element: Element) {
    element.getElementsByTag("img").forEach { child ->
        child.attr("src", child.attr("abs:src"))
    }
}
