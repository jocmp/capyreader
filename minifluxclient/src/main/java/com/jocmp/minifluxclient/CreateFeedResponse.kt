package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateFeedResponse(
    val feed_id: Long
)
