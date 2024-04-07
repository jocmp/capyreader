package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import com.jocmp.basil.common.escapingSpecialXMLCharacters
import com.jocmp.basil.common.prepending

internal fun Feed.asOPML(indentLevel: Int): String {
    val parsedSiteURL = siteURL.escapingSpecialXMLCharacters
    val parsedFeedURL = feedURL.escapingSpecialXMLCharacters
    val parsedName = title.escapingSpecialXMLCharacters

    val opml =
        "<outline text=\"${parsedName}\" title=\"${parsedName}\" description=\"\" type=\"rss\" version=\"RSS\" htmlUrl=\"${parsedSiteURL}\" xmlUrl=\"${parsedFeedURL}\" basil_id=\"${id}\"/>\n"
    return opml.prepending(tabCount = indentLevel)
}
