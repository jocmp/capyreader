package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateTaggingRequest(
    val feed_id: String,
    val name: String,
)
