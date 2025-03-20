package com.jocmp.feverclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupsResult(
    val groups: List<Group>?,
    val feed_groups: List<FeedGroup>?
)
