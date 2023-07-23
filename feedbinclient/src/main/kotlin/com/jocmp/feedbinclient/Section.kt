package com.jocmp.feedbinclient

sealed class Section {
    class FolderSection(val folder: Folder) : Section()

    class FeedSection(val feeds: List<Feed>) : Section()
}
