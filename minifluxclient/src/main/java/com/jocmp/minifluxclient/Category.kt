package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    val id: Long,
    val title: String,
    val user_id: Long,
    val feed_count: Int? = null
)
