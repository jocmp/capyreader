package com.jocmp.mallet

private val youtubePatterns = listOf(
    Regex("//(?:www\\.)?youtube-nocookie\\.com/embed/(.*?)(?:\\?|$)"),
    Regex("//(?:www\\.)?youtube\\.com/embed/(.*?)(?:\\?|$)"),
    Regex("//www\\.youtube\\.com/user/.*?#\\w/\\w/\\w/\\w/(.+)\\b"),
    Regex("//(?:www\\.)?youtube\\.com/v/(.*?)(?:#|\\?|$)"),
    Regex("//(?:www\\.)?youtube\\.com/watch\\?(?:.*?&)?v=([^&#]*)"),
    Regex("//youtu\\.be/(.*?)(?:\\?|$)"),
)

internal fun getVideo(src: String?): Video? {
    if (src == null) {
        return null
    }

    val videoId = youtubeVideoId(src) ?: return null

    return Video(
        src = src,
        imageUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
        link = "https://www.youtube.com/watch?v=$videoId",
    )
}

private fun youtubeVideoId(src: String): String? {
    return youtubePatterns.firstNotNullOfOrNull { pattern ->
        pattern.find(src)?.groupValues?.get(1)?.ifBlank { null }
    }
}

internal data class Video(
    val src: String,
    val imageUrl: String,
    val link: String,
) {
    val width: Int = 480
    val height: Int = 360
}
