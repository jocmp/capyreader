package com.jocmp.basil

import com.jocmp.basil.extensions.prepending

data class Feed(
    val id: String,
    val name: String
): Comparable<Feed> {
    override fun compareTo(other: Feed): Int {
        return when {
            this.name != other.name -> this.name.compareTo(other.name)
            else -> 0
        }
    }
}

fun Feed.asOPML(indentLevel: Int): String {
    val opml = "<outline text=\"${name}\" title=\"${name}\" description=\"\" type=\"rss\" version=\"RSS\" htmlUrl=\"\" xmlUrl=\"\"/>\n"
    return opml.prepending(tabCount = indentLevel)
}
