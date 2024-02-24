package com.jocmp.basil.accounts

import java.time.OffsetDateTime

internal data class ParsedItem(
    val id: String,
    val title: String? = null,
    val contentHTML: String? = null,
    val url: String? = null,
    val summary: String? = null,
    val imageURL: String? = null,
    val publishedAt: OffsetDateTime?
)
