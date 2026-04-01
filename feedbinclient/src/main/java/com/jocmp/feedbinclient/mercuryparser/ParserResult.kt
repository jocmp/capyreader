package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParserResult(
    val title: String?,
    val author: String?,
    val content: String?,
    val excerpt: String?,
    val lead_image_url: String?,
)
