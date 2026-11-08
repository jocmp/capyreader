package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarredStoryHashesResponse(
    val starred_story_hashes: List<String> = emptyList(),
    val authenticated: Boolean = false,
)
