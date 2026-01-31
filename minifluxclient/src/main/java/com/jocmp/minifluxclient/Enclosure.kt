package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Enclosure(
    val id: Long,
    val user_id: Long,
    val entry_id: Long,
    val url: String,
    val mime_type: String,
    val size: Long
)
