package com.jocmp.basil

import com.jocmp.basil.shared.prepending

data class Feed(
    val id: String,
    val externalID: String,
    val name: String,
    val feedURL: String,
    val siteURL: String = "",
    val unreadCount: Long = 0
) {
    internal val primaryKey: Long
        get() = id.toLong()

    override fun equals(other: Any?): Boolean {
        if (other is Feed) {
            return id == other.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
