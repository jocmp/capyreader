package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Icon(
    val feed_id: Long,
    val icon_id: Long
)
