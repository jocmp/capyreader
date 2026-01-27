package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EntryResultSet(
    val total: Int,
    val entries: List<Entry>
)
