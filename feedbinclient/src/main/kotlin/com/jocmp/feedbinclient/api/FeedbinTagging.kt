package com.jocmp.feedbinclient.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedbinTagging(
    val id: Int,
    val feed_id: Int,
    val name: String
)