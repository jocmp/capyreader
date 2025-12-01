package com.jocmp.capy.articles

import com.jocmp.capy.Article

fun Article.imageEnclosuresHtml(): String {
    val images = enclosures.filter { it.type.startsWith("image/") }

    if (images.isEmpty()) {
        return ""
    }

    return buildString {
        append("<div>")
        images.forEach { enclosure ->
            append("""<img src="${enclosure.url}" loading="lazy">""")
        }
        append("</div>")
    }
}
