package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.common.escapingSpecialHTMLCharacters

fun Article.enclosureHTML(): String {
    val images = imageEnclosureHTML()
    val videos = videoEnclosureHTML()

    return images + videos
}

fun Article.audioEnclosureHTML(
    playLabel: String = "Play",
    pauseLabel: String = "Pause",
): String {
    val audioItems = enclosures.filter { it.type.startsWith("audio/") }

    if (audioItems.isEmpty()) {
        return ""
    }

    return buildString {
        audioItems.forEach { enclosure ->
            val durationFormatted =
                enclosure.itunesDurationSeconds?.let { formatDuration(it) }.orEmpty()
            val artworkUrl = enclosure.itunesImage.orEmpty()
            val rawUrl = enclosure.url.toString()
            val escapedTitle = title.escapeForJs()
            val escapedFeedName = feedName.escapeForJs()
            val escapedArtworkUrl = artworkUrl.escapeForJs()
            val escapedUrl = rawUrl.escapeForJs()

            append(
                """
                <div class="audio-enclosure" data-url="${rawUrl.escapeForHtml()}">
                    <div class="audio-enclosure__artwork">
                        ${if (artworkUrl.isNotEmpty()) """<img src="$artworkUrl" loading="lazy">""" else """<div class="audio-enclosure__artwork-placeholder"></div>"""}
                    </div>
                    <div class="audio-enclosure__content">
                        <div class="audio-enclosure__title">${title.escapeForHtml()}</div>
                        <div class="audio-enclosure__feed">${feedName.escapeForHtml()}</div>
                        ${if (durationFormatted.isNotEmpty()) """<div class="audio-enclosure__duration">$durationFormatted</div>""" else ""}
                    </div>
                    <div class="audio-enclosure__play-button" role="button" aria-label="${playLabel.escapeForHtml()}" data-play-label="${playLabel.escapeForHtml()}" data-pause-label="${pauseLabel.escapeForHtml()}" onclick="playAudio('$escapedUrl', '$escapedTitle', '$escapedFeedName', ${enclosure.itunesDurationSeconds ?: "null"}, '$escapedArtworkUrl')">
                        <div class="audio-enclosure__play-icon"></div>
                    </div>
                </div>
            """.trimIndent()
            )
        }
    }
}

private fun Article.imageEnclosureHTML(): String {
    val images = enclosures
        .filter { it.type.startsWith("image/") }
        .filterNot { enclosure ->
            val url = enclosure.url.toString()

            content.contains(url) ||
                    content.contains(url.escapingSpecialHTMLCharacters)
        }

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

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%d:%02d".format(minutes, secs)
    }
}

private fun String.escapeForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}

private fun String.escapeForHtml(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
