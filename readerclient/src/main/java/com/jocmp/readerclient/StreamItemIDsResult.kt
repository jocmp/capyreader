package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamItemIDsResult(
    val itemRefs: List<ItemRef>,
    val continuation: String?,
)
