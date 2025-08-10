package com.jocmp.capy

import java.net.URL
import java.time.ZonedDateTime

data class Article(
    val id: String,
    val feedID: String,
    val title: String,
    val author: String?,
    val contentHTML: String,
    val url: URL?,
    val summary: String,
    val imageURL: String?,
    val updatedAt: ZonedDateTime,
    val publishedAt: ZonedDateTime,
    val read: Boolean,
    val starred: Boolean,
    val feedName: String = "",
    val faviconURL: String? = null,
    val feedURL: String? = null,
    val siteURL: String? = null,
    val enableStickyFullContent: Boolean = false,
    val openInBrowser: Boolean = false,
    val fullContent: FullContent = FullContent.None,
    val content: String = contentHTML.ifBlank { summary },
    val enclosures: List<Enclosure> = emptyList(),
) {
    val defaultContent = contentHTML.ifBlank { summary }

    val parseFullContent = fullContent is FullContent.Loaded
}

sealed class FullContent {
    object None : FullContent()

    class Loading(val articleID: String) : FullContent()

    class Loaded(val articleID: String, val content: String) : FullContent()

    class Error(val articleID: String) : FullContent()
}