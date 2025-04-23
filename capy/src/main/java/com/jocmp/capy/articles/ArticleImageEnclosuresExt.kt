package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.jsoup.nodes.Element

fun Article.imageEnclosures(): Element? {
    val images = enclosures.filter { it.type.startsWith("image/") }

    if (images.isEmpty()) {
        return null
    }

    return Element("div").apply {
        enclosures.forEach { enclosure ->
            val image = Element("img").apply {
                attr("src", enclosure.url.toString())
            }

            appendChild(image)
        }
    }
}
