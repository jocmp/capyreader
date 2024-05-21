package com.jocmp.basil

import java.net.URL
import java.time.ZonedDateTime

data class Article(
    val id: String,
    val feedID: String,
    val title: String,
    val author: String?,
    val contentHTML: String,
    val extractedContentURL: URL?,
    val url: URL,
    val summary: String,
    val imageURL: URL?,
    val updatedAt: ZonedDateTime,
    val publishedAt: ZonedDateTime,
    val read: Boolean,
    val starred: Boolean,
    val feedName: String = "",
    val faviconURL: String? = null,
    val extractedContent: String? = null
)
