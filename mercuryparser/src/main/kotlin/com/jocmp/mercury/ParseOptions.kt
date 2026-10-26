package com.jocmp.mercury

import okhttp3.OkHttpClient

enum class ContentType { HTML, MARKDOWN, TEXT }

data class ParseOptions(
    val html: String? = null,
    val fetchAllPages: Boolean = true,
    val fallback: Boolean = true,
    val contentType: ContentType = ContentType.HTML,
    val headers: Map<String, String> = emptyMap(),
    val httpClient: OkHttpClient? = null,
)
