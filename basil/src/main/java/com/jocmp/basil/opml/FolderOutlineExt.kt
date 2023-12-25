package com.jocmp.basil.opml

import com.jocmp.basil.Folder
import com.jocmp.basil.db.Feeds as DBFeed

internal fun Outline.FolderOutline.asFolder(feeds: Map<Long, DBFeed>): Folder {
    return Folder(
        title = folder.title ?: "",
        feeds = folder.feeds.mapNotNull {
            it.asFeed(feeds = feeds)
        }.toMutableList()
    )
}
