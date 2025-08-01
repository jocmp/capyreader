package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreatePageRequest(
    val url: String,
    val title: String? = null
)