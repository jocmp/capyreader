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
    val categories: List<String>? = null
) {
    val hexID = id.split("/").last()

    val read: Boolean
        get() = isRead(categories)

    val starred: Boolean
        get() = isStarred(categories)

    @JsonClass(generateAdapter = true)
    data class Origin(
        val streamId: String,
        val htmlUrl: String,
        val title: String,
    )

    @JsonClass(generateAdapter = true)
    data class Summary(
        val content: String?,
    )

    @JsonClass(generateAdapter = true)
    data class Content(
        val content: String,
    )

    @JsonClass(generateAdapter = true)
    data class Enclosure(
        val href: String?,
        val type: String?,
    )

    @JsonClass(generateAdapter = true)
    data class Link(
        val href: String
    )

    val images: List<Enclosure>
        get() = enclosure?.filter { it.type?.startsWith("image") == true }.orEmpty()
}

/** open for testing */
internal fun isRead(categories: List<String>?): Boolean {
    return categories?.any { it.endsWith("state/com.google/read") } ?: false
}

/** open for testing */
internal fun isStarred(categories: List<String>?): Boolean {
    return categories?.any { it.endsWith("state/com.google/starred") } ?: false
}
