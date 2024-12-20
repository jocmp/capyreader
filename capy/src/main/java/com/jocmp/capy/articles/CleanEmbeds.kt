package com.jocmp.capy.articles

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal fun cleanEmbeds(document: Document) {
    document.select("iframe").forEach { embed ->
        val src = embed.attr("src")
        val youtubeID = findYouTubeMatch(src)

        if (youtubeID != null) {
            swapPlaceholder(document, embed, src, youtubeID)
        }
    }
}

fun findYouTubeMatch(src: String): String? {
    YOUTUBE_DOMAINS.forEach {
        val match = it.find(src)

        if (match != null) {
            return match.groupValues.getOrNull(1)
        }
    }

    return null
}

private fun swapPlaceholder(document: Document, embed: Element, src: String, youtubeID: String) {
    val placeholderImage = document.createElement("img").apply {
        addClass("iframe-embed__image")
        attr("src", imageURL(youtubeID))
    }

    val playButton = document.createElement("div").apply {
        addClass("iframe-embed__play-button")
    }

    val placeholder = document.createElement("div").apply {
        addClass("iframe-embed")
        attr("data-iframe-src", autoplaySrc(src))
        appendChild(placeholderImage)
        appendChild(playButton)
    }

    embed.replaceWith(placeholder)
}

private fun imageURL(id: String): String {
    return "https://img.youtube.com/vi/$id/hqdefault.jpg"
}

private fun autoplaySrc(src: String): String {
    return try {
        src.toHttpUrl()
            .newBuilder()
            .setQueryParameter("autoplay", "1")
            .toString()
    } catch (e: IllegalArgumentException) {
        src
    }
}

private val YOUTUBE_DOMAINS = listOf(
    Regex(""".*?//www\.youtube-nocookie\.com/embed/(.*?)(\?|$)"""),
    Regex(""".*?//www\.youtube\.com/embed/(.*?)(\?|$)"""),
    Regex(""".*?//www\.youtube\.com/user/.*?#\w/\w/\w/\w/(.+)\b"""),
    Regex(""".*?//www\.youtube\.com/v/(.*?)(#|\?|$)"""),
    Regex(""".*?//www\.youtube\.com/watch\?(?:.*?&)?v=([^&#]*)(?:&|#|$)"""),
    Regex(""".*?//youtube-nocookie\.com/embed/(.*?)(\?|$)"""),
    Regex(""".*?//youtube\.com/embed/(.*?)(\?|$)"""),
    Regex(""".*?//youtu\.be/(.*?)(\?|$)""")
)
