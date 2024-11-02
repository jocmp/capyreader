package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Subscription(
   val id: String,
    val title: String,
    val categories: List<Category>,
    val url: String,
    val htmlUrl: String,
    val iconUrl: String,
)
