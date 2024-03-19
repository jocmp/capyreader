package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Icon(
    val host: String,
    val url: String,
)
