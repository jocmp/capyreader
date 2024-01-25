package com.jocmp.basil.opml

import com.jocmp.basil.Account
import com.jocmp.basil.OPMLFile
import java.net.URI

/**
 * 1. Load file via `OPMLFile`
 * 2. Normalize feeds
 * 3. When normalized, iterate through each feed and call `addFeed` on account
 */
class OPMLImporter(private val account: Account) {
    private val normalizedItems = mutableListOf<Outline>()

    suspend fun import(uri: URI) {
        val file = OPMLFile(path = uri, account = account).load()

        file
    }

    private fun normalize(items: List<Outline>, parent: Outline.FolderOutline? = null) {
        val feedsToAdd = mutableListOf<Outline>()

        items.forEach { item ->
            if (item is Outline.FeedOutline && feedsToAdd.none(containsFeed(item))) {
                feedsToAdd.add(item)
            } else if (item is Outline.FolderOutline) {
                if (item.folder.title.isNullOrBlank()) {

                } else {
                    feedsToAdd.add(item)
                }
            }
        }
    }

//    private fun normalizeFolder(feeds: )
}

private fun containsFeed(item: Outline.FeedOutline): (Outline) -> Boolean {
    return { outline: Outline ->
        (outline as Outline.FeedOutline?)?.feed?.xmlUrl == item.feed.xmlUrl
    }
}
