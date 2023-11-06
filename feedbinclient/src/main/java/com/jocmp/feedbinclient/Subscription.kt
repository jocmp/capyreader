package com.jocmp.feedbinclient.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Subscription(
    val id: Long,
    val created_at: String,
    val feed_id: Long,
    val title: String,
    val feed_url: String,
    val site_url: String
)
