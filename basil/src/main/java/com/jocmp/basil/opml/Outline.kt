package com.jocmp.basil.opml

internal sealed class Outline {
    class FolderOutline(val folder: Folder) : Outline()

    class FeedOutline(val feed: Feed) : Outline()
}
