package com.jocmp.basil

import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
data class Feed(
    val id: String,
    val externalID: String,
    val name: String,
    val feedURL: String,
    val siteURL: String = "",
    override val count: Long = 0
): Countable {
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
