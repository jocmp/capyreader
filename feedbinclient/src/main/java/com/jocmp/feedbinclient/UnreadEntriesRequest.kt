package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnreadEntriesRequest(
    val unread_entries: List<Long>,
)
