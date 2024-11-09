package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionQuickAddResult(
    val numResults: Int?,
    val query: String? = null,
    val streamId: String? = null,
    val streamName: String? = null,
)
