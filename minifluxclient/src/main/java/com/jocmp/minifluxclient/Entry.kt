package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Entry(
    val id: Long,
    val user_id: Long,
    val feed_id: Long,
    val status: EntryStatus,
    val hash: String,
    val title: String,
    val url: String,
    val comments_url: String?,
    val published_at: String,
    val created_at: String,
    val changed_at: String,
    val content: String,
    val author: String?,
    val share_code: String?,
    val starred: Boolean,
    val reading_time: Int,
    val enclosures: List<Enclosure>? = null,
    val feed: Feed? = null
)
