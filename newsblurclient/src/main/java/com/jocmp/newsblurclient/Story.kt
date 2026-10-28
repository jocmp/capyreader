package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Story(
    val story_hash: String,
    val story_feed_id: Long,
    val story_title: String,
    val story_content: String? = null,
    val story_permalink: String? = null,
    val story_authors: String? = null,
    val story_timestamp: String,
    val image_urls: List<String> = emptyList(),
    val read_status: Int = 0,
    val starred: Boolean = false,
)
