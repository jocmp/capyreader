package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateTagRequest(
    val old_name: String,
    val new_name: String
)
