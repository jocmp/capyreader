package com.jocmp.feverclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedGroup(
    val group_id: Int,
    val feed_ids: String,
) {
    val feedIDs
        get() = feed_ids.split(",")
}
