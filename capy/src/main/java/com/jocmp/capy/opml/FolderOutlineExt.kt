package com.jocmp.capy.opml

import com.jocmp.capy.db.Feeds as DBFeed
import com.jocmp.capy.Folder

internal fun Outline.FolderOutline.asFolder(feeds: Map<Long, DBFeed>): Folder {
    return Folder(
        title = folder.title ?: "",
        feeds = folder.feeds.mapNotNull {
            it.asFeed(feeds = feeds)
        }.toMutableList()
    )
}
