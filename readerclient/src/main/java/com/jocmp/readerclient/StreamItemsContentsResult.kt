package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamItemsContentsResult(
    val items: List<Item>,
)
