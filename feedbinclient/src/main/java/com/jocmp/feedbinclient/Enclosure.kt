package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Enclosure(
    val enclosure_url: String,
    val enclosure_type: String,
    val enclosure_length: String?,
    val itunes_duration: String?,
    val itunes_image: String?,
)
