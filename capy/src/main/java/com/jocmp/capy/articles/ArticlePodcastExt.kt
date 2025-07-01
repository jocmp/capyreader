package com.jocmp.capy.articles

import com.jocmp.capy.Article

fun Article.podcast(): Podcast? {
    val enclosure = enclosures.firstOrNull { it.type.startsWith("audio/") } ?: return null

    return Podcast(articleID = id, title = title, feedName = feedName, enclosure = enclosure)
}
