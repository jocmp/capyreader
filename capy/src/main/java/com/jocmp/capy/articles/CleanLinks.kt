package com.jocmp.capy.articles

import org.jsoup.nodes.Element

internal fun cleanLinks(element: Element) {
    element.getElementsByTag("img").forEachIndexed{ index, child ->
        child.attr("src", child.absUrl("src"))

        if (index > 0) {
            child.attr("loading", "lazy")
        }
    }

    element.select("img[data-src]").forEach { child ->
        child.attr("src", child.absUrl("data-src"))
    }
}
