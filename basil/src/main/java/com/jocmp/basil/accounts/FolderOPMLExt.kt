package com.jocmp.basil.accounts

import com.jocmp.basil.Folder
import com.jocmp.basil.shared.prepending
import com.jocmp.basil.shared.repeatTab

internal fun Folder.asOPML(indentLevel: Int = 0): String {
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
