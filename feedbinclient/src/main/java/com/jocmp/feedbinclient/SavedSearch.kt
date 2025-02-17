package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavedSearch(
    val id: Long,
    val name: String,
    val query: String,
)
