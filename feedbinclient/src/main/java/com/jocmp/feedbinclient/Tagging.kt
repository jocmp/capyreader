package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tagging(
    val id: Long,
    val feed_id: Long,
    val name: String
)
