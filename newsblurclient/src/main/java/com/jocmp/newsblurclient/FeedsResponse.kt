package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedsResponse(
    val feeds: Map<String, Feed> = emptyMap(),
    val flat_folders: Map<String, List<Long>> = emptyMap(),
)
