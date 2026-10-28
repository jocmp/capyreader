package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddURLResponse(
    val code: Int,
    val message: String? = null,
    val feed: Feed? = null,
)
