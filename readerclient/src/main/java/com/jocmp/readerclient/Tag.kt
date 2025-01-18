package com.jocmp.readerclient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tag(
    val id: String,
    val type: Type?,
) {
    enum class Type {
        @Json(name = "folder")
        FOLDER,
        @Json(name = "tag")
        TAG
    }
}
