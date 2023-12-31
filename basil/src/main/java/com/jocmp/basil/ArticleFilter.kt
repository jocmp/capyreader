package com.jocmp.basil

sealed class ArticleFilter(open val status: Status) {
    enum class Status(value: String) {
        ALL("all"),
        UNREAD("unread"),
        STARRED("starred")
    }

    fun withStatus(status: Status): ArticleFilter {
        return when (this) {
            is Articles -> copy(status = status)
            is Feeds -> copy(status = status)
            is Folders -> copy(status = status)
        }
    }

    data class Articles(override val status: Status) : ArticleFilter(status)

    data class Feeds(val feed: Feed, override val status: Status) : ArticleFilter(status)

    data class Folders(val folder: Folder, override val status: Status) : ArticleFilter(status)

    companion object {
        fun default() = Articles(status = Status.ALL)
    }
}
