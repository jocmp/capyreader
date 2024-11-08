package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionQuickAddResult(
    val numResults: Int,
    val query: String,
    val streamId: String,
)
