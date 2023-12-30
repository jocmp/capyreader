package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import com.jocmp.basil.shared.prepending

fun Feed.asOPML(indentLevel: Int): String {
    val opml =
        "<outline text=\"${name}\" title=\"${name}\" description=\"\" type=\"rss\" version=\"RSS\" htmlUrl=\"${siteURL}\" xmlUrl=\"${feedURL}\" basil_id=\"${id}\"/>\n"
    return opml.prepending(tabCount = indentLevel)
}
