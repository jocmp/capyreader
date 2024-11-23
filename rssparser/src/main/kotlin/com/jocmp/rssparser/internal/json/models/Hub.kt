package com.jocmp.rssparser.internal.json.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Hub(
    val type: String,
    val url: String,
)
