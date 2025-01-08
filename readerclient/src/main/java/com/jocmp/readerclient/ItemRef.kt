package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemRef(
    val id: String,
) {
    val hexID = buildHexID(id)
}

fun buildHexID(id: String) = String.format("%016x", id.toLong())
