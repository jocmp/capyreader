package com.jocmp.feverclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Group(
    val id: Int,
    val title: String,
)
