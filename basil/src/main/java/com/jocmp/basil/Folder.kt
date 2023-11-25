package com.jocmp.basil

import com.jocmp.basil.extensions.prepending
import com.jocmp.basil.extensions.repeatTab

data class Folder(
    val title: String,
    val feeds: MutableList<Feed> = mutableListOf()
) : Comparable<Folder> {
    override fun compareTo(other: Folder): Int {
        return when {
            this.title != other.title -> this.title.compareTo(other.title)
            else -> 0
        }
    }
}

fun Folder.asOPML(indentLevel: Int = 0): String {
    if (feeds.isEmpty()) {
        val opml = "<outline text=\"${title}\" title=\"${title}\"/>\n"
        return opml.prepending(tabCount = indentLevel)
    }

    var opml = "<outline text=\"${title}\" title=\"${title}\">\n"
    opml = opml.prepending(tabCount = indentLevel)

    feeds.forEach { feed ->
        opml += feed.asOPML(indentLevel = indentLevel + 1)
    }

    opml = opml + repeatTab(tabCount = indentLevel) + "</outline>\n"

    return opml
}
