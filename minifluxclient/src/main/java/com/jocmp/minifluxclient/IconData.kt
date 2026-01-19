package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IconData(
    val id: Long,
    val data: String,
    val mime_type: String
)
