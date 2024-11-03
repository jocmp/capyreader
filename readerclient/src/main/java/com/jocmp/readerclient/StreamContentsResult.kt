package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamContentsResult(
    val items: List<Item>,
    val continuation: String? = null,
)
