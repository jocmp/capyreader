package com.jocmp.basil.extensions

import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.opml.Outline

internal val Outline.FolderOutline.asFolder: Folder
    get() {
        return Folder(
            title = folder.title ?: "",
            feeds = folder.feeds.map { feed ->
                Feed(id = "", name = feed.title ?: "")
            }.toMutableList()
        )
    }
