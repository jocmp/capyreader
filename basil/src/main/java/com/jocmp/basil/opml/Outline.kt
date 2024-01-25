package com.jocmp.basil.opml

internal sealed class Outline {
    abstract val title: String

    data class FolderOutline(val folder: Folder) : Outline() {
        override val title: String
            get() = folder.title ?: ""
    }

    data class FeedOutline(val feed: Feed) : Outline() {
        override val title: String
            get() = feed.title ?: ""
    }
}
