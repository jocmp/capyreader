package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionChoice(
    val feed_url: String,
    val title: String,
)
