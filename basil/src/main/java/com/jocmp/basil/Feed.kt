package com.jocmp.basil

import com.jocmp.basil.extensions.prepending

data class Feed(
    val id: String,
    val name: String,
    val feedURL: String,
    val homepageURL: String = ""
) {
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

fun Feed.asOPML(indentLevel: Int): String {
    val opml = "<outline text=\"${name}\" title=\"${name}\" description=\"\" type=\"rss\" version=\"RSS\" htmlUrl=\"\" xmlUrl=\"${feedURL}\"/>\n"
    return opml.prepending(tabCount = indentLevel)
}
