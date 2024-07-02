package com.jocmp.capy.accounts

import com.jocmp.capy.Feed
import com.jocmp.capy.common.escapingSpecialXMLCharacters
import com.jocmp.capy.common.prepending

internal fun Feed.asOPML(indentLevel: Int): String {
    val parsedSiteURL = siteURL.escapingSpecialXMLCharacters
    val parsedFeedURL = feedURL.escapingSpecialXMLCharacters
    val parsedName = title.escapingSpecialXMLCharacters

    val opml =
        "<outline text=\"${parsedName}\" title=\"${parsedName}\" description=\"\" type=\"rss\" version=\"RSS\" htmlUrl=\"${parsedSiteURL}\" xmlUrl=\"${parsedFeedURL}\"/>\n"
    return opml.prepending(tabCount = indentLevel)
}
