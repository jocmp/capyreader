package com.jocmp.basil

import java.net.URL
import java.time.OffsetDateTime

data class Article(
    val id: String,
    val feedID: String,
    val title: String,
    val contentHTML: String,
    val url: URL?,
    val summary: String,
    val imageURL: URL?,
    val updatedAt: OffsetDateTime,
    val read: Boolean,
    val starred: Boolean,
    val feedName: String = "",
)
