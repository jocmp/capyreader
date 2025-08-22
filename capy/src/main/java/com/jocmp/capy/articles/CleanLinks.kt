package com.jocmp.capy.articles

import org.jsoup.nodes.Element

internal fun cleanLinks(element: Element) {
    element.getElementsByTag("img").forEachIndexed { index, child ->
        if (index == 0) {
            child.attr("fetchpriority", "high")
        } else {
            child.attr("loading", "lazy")
        }

        child.attr("src", child.attr("abs:src"))
    }

    element.select("img[data-src]").forEach { child ->
        child.attr("src", child.attr("data-src"))
    }

    extractChildImages(element)
}

private fun extractChildImages(document: Element) {
    try {
        document.select("a img").forEach {
            attachImageToAnchorParent(it, it.parent())
        }
    } catch (e: StackOverflowError) {
        return
    }
}

private fun attachImageToAnchorParent(img: Element, parent: Element?) {
    if (parent == null || parent.tagName() == "body") {
        return;
    } else if (parent.tagName() == "a") {
        parent.parent()?.apply { appendChild(img) }
        parent.remove()
    } else {
        attachImageToAnchorParent(img, parent.parent())
    }
}
