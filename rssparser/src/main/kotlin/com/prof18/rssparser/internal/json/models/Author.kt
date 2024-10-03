package com.prof18.rssparser.internal.json.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Author(
    val name: String?,
    val url: String?,
    val avatar: String?,
)
