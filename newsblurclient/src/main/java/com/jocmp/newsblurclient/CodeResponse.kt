package com.jocmp.newsblurclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CodeResponse(
    val code: Int,
    val message: String? = null,
)
