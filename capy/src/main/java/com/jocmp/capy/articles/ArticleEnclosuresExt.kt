package com.jocmp.capy.articles

import com.jocmp.capy.Article

fun Article.enclosureHTML(): String {
    val images = imageEnclosureHTML()
    val videos = videoEnclosureHTML()

    return images + videos
}

private fun Article.imageEnclosureHTML(): String {
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

private fun Article.videoEnclosureHTML(): String {
    val videos = enclosures.filter { it.type.startsWith("video/") }

    if (videos.isEmpty()) {
        return ""
    }

    return buildString {
        videos.forEach { enclosure ->
            append("""<video src="${enclosure.url}#t=0.001" controls preload="metadata"></video>""")
        }
    }
}
