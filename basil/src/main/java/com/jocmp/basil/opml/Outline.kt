package com.jocmp.basil.opml

import com.jocmp.basil.Feed
import com.jocmp.basil.Folder

sealed class Outline {
    class FolderOutline(val folder: Folder) : Outline()

    class FeedOutline(val feed: Feed) : Outline()
}
