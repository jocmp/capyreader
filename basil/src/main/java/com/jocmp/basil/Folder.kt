package com.jocmp.basil

import com.jocmp.basil.shared.prepending
import com.jocmp.basil.shared.repeatTab

data class Folder(
    val title: String,
    val feeds: MutableList<Feed> = mutableListOf()
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
