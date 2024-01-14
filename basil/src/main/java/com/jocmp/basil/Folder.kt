package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val title: String,
    val feeds: MutableList<Feed> = mutableListOf(),
    val unreadCount: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (other is Folder) {
            return title == other.title
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        val result = title.hashCode()
        return 31 * result
    }
}
