package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionListResult(
    val subscriptions: List<Subscription>
)
