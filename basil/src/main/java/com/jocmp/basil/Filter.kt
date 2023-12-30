package com.jocmp.basil

sealed class Filter(val status: Status) {
    enum class Status(value: String) {
        ALL("all"),
        READ("read"),
        STARRED("starred")
    }

    class Articles(status: Status) : Filter(status)

    class Feeds(val feed: Feed, status: Status) : Filter(status)

    class Folders(val folder: Folder, status: Status) : Filter(status)
}
