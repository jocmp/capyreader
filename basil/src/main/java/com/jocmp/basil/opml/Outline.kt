package com.jocmp.basil.opml

internal sealed class Outline {
    abstract val title: String

    class FolderOutline(val folder: Folder) : Outline() {
        override val title: String
            get() = folder.title ?: ""
    }

    class FeedOutline(val feed: Feed) : Outline() {
        override val title: String
            get() = feed.title ?: ""
    }
}
