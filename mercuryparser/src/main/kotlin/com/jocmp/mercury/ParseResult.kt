package com.jocmp.mercury

import java.time.Instant

data class ParseResult(
    val title: String? = null,
    val author: String? = null,
    val datePublished: Instant? = null,
    val dek: String? = null,
    val leadImageUrl: String? = null,
    val content: String? = null,
    val nextPageUrl: String? = null,
    val url: String = "",
    val domain: String = "",
    val excerpt: String? = null,
    val wordCount: Int = 0,
    val direction: String? = null,
    val totalPages: Int = 1,
    val renderedPages: Int = 1,
    val error: Boolean = false,
    val message: String? = null,
)
