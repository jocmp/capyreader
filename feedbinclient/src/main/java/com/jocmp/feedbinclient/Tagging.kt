package com.jocmp.feedbinclient.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tagging(
    val id: Int,
    val feed_id: Int,
    val name: String
)
