package com.jocmp.basil

import com.jocmp.basil.extensions.asFolder
import com.jocmp.basil.opml.Outline
import java.net.URI

data class Account(
    val id: String,
    val path: URI,
) {
    var folders = mutableSetOf<Folder>()
        private set

    val opmlFile = OPMLFile(
        path = path.resolve("subscriptions.opml"),
        account = this,
    )

    internal fun loadOPMLItems(items: List<Outline>) {
        items.forEach { item ->
            when (item) {
                is Outline.FolderOutline -> folders.add(item.asFolder)
                is Outline.FeedOutline -> print("Feed for ya")
            }
        }
    }
}
