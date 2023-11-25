package com.jocmp.basil

import com.jocmp.basil.extensions.asFeed
import com.jocmp.basil.extensions.asFolder
import com.jocmp.basil.opml.Outline
import java.net.URI

data class Account(
    val id: String,
    val path: URI,
) {
    var folders = mutableSetOf<Folder>()
        private set

    var feeds = mutableSetOf<Feed>()
        private set

    val opmlFile = OPMLFile(
        path = path.resolve("subscriptions.opml"),
        account = this,
    )

    val displayName = "Test Display Name"

    internal fun loadOPMLItems(items: List<Outline>) {
        items.forEach { item ->
            when (item) {
                is Outline.FolderOutline -> folders.add(item.asFolder)
                is Outline.FeedOutline -> item.asFeed
            }
        }
    }
}

fun Account.asOPML(): String {
    var opml = ""

    feeds.sorted().forEach { feed ->
        opml += feed.asOPML(indentLevel = 2)
    }

    folders.sorted().forEach { folder ->
        opml += folder.asOPML(indentLevel = 2)
    }

    return opml
}
