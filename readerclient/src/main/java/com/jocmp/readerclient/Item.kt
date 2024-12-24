package com.jocmp.readerclient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Item(
    val id: String,
    val published: Long,
    val title: String,
    val canonical: List<Link>,
    val summary: Summary,
    val origin: Origin,
    val content: Content? = null,
    val author: String? = null,
    val enclosure: List<Enclosure>? = null,
    val categories: List<Category>? = null
) {
    val hexID = id.split("/").last()

    val read: Boolean
        get() = categories?.any { it.id == Stream.READ.id } ?: true

    @JsonClass(generateAdapter = true)
    data class Origin(
        val streamId: String,
        val htmlUrl: String,
        val title: String,
    )

    @JsonClass(generateAdapter = true)
    data class Summary(
        val content: String,
    )

    @JsonClass(generateAdapter = true)
    data class Content(
        val content: String,
    )

    @JsonClass(generateAdapter = true)
    data class Enclosure(
        val href: String,
        val type: String,
    )

    @JsonClass(generateAdapter = true)
    data class Link(
        val href: String
    )

    val image: Enclosure?
        get() = enclosure?.find { it.type.startsWith("image") }
}
