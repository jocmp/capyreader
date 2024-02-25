package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarredEntriesRequest(
    val starred_entries: List<Long>,
)
