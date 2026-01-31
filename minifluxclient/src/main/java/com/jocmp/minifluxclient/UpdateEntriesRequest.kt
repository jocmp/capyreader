package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateEntriesRequest(
    val entry_ids: List<Long>,
    val status: EntryStatus
)
