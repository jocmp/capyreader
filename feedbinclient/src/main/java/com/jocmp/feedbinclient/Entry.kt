package com.jocmp.feedbinclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Entry(
    val id: Long,
    val feed_id: Long,
    val title: String?,
    val url: String?,
    val extracted_content_url: String?,
    val author: String?,
    val content: String?,
    val summary: String?,
    val published: String,
    val created_at: String,
    val images: Images? = null,
) {
    @JsonClass(generateAdapter = true)
    class Images(
        val size_1: SizeOne,
    ) {
        @JsonClass(generateAdapter = true)
        class SizeOne(
            val cdn_url: String,
        )
    }
}
