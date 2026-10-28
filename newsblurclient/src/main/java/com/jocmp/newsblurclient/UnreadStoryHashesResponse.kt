package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnreadStoryHashesResponse(
    val unread_feed_story_hashes: Map<String, List<String>> = emptyMap(),
    val authenticated: Boolean = false,
)
