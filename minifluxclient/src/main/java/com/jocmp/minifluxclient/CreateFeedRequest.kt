package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateFeedRequest(
    val feed_url: String,
    val category_id: Long? = null
)
