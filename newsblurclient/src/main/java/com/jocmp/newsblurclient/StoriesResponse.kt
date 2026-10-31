package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoriesResponse(
    val stories: List<Story> = emptyList(),
)
