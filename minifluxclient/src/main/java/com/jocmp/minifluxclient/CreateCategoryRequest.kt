package com.jocmp.minifluxclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val title: String
)
