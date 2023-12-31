package com.jocmp.basil

sealed class ArticleFilter(open val status: Status) {
    enum class Status(value: String) {
        ALL("all"),
        READ("read"),
        STARRED("starred")
    }

    data class Articles(override val status: Status) : ArticleFilter(status)

    data class Feeds(val feed: Feed, override val status: Status) : ArticleFilter(status)

    data class Folders(val folder: Folder, override val status: Status) : ArticleFilter(status)

    companion object {
        fun default() = ArticleFilter.Articles(status = Status.ALL)
    }
}
