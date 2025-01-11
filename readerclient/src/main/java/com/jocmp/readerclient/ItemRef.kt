package com.jocmp.readerclient

import com.jocmp.readerclient.ItemIdentifiers.parseToHexID
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemRef(
    val id: String,
) {
    val hexID = parseToHexID(id)
}
