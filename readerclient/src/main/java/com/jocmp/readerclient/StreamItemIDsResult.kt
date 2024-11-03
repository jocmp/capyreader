package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamItemIDsResult(
    val itemRefs: List<ItemRef>
) {
    @JsonClass(generateAdapter = true)
    data class ItemRef(
        val id: Long,
    ) {
        val hexID = String.format("%016x", id)
    }
}
