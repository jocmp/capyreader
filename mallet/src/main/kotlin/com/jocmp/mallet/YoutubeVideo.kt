package com.jocmp.mallet

private val youtubeIdPattern = "youtube.com/embed/([^?/]*)".toRegex()

internal fun getVideo(src: String?): Video? {
    if (src == null) {
        return null
    }
    val videoId = youtubeIdPattern.find(src)?.groupValues?.get(1) ?: return null

    return Video(
        src = src,
        imageUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
        link = "https://www.youtube.com/watch?v=$videoId",
    )
}

internal data class Video(
    val src: String,
    val imageUrl: String,
    val link: String,
) {
    val width: Int = 480
    val height: Int = 360
}
