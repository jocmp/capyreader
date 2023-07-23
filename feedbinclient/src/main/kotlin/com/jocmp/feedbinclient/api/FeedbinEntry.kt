package com.jocmp.feedbinclient.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedbinEntry(
    val id: Long,
    val feed_id: Long,
    val title: String?,
    val url: String?,
    val extracted_content_url: String?,
    val author: String?,
    val content: String?,
    val summary: String?,
    val published: String,
    val created_at: String
)
