package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSubscriptionRequest(
    val feed_url: String
)
