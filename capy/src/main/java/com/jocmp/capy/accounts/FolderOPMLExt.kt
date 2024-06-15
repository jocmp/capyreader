package com.jocmp.capy.accounts

import com.jocmp.capy.Folder
import com.jocmp.capy.common.escapingSpecialXMLCharacters
import com.jocmp.capy.common.prepending
import com.jocmp.capy.common.repeatTab

internal fun Folder.asOPML(indentLevel: Int = 0): String {
    val parsedTitle = title.escapingSpecialXMLCharacters

    if (feeds.isEmpty()) {
        val opml = "<outline text=\"${parsedTitle}\" title=\"${parsedTitle}\"/>\n"
        return opml.prepending(tabCount = indentLevel)
    }

    var opml = "<outline text=\"${parsedTitle}\" title=\"${parsedTitle}\">\n"
    opml = opml.prepending(tabCount = indentLevel)

    feeds.forEach { feed ->
        opml += feed.asOPML(indentLevel = indentLevel + 1)
    }

    opml = opml + repeatTab(tabCount = indentLevel) + "</outline>\n"

    return opml
}
