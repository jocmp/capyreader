package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feed(
    val id: Long,
    val feed_title: String,
    val feed_address: String,
    val feed_link: String? = null,
    val favicon_url: String? = null,
)
