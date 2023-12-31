package com.jocmp.basil

sealed class ArticleFilter(open val status: ArticleStatus) {
    fun withStatus(status: ArticleStatus): ArticleFilter {
        return when (this) {
            is Articles -> copy(status = status)
            is Feeds -> copy(status = status)
            is Folders -> copy(status = status)
        }
    }

    data class Articles(override val status: ArticleStatus) : ArticleFilter(status)

    data class Feeds(val feed: Feed, override val status: ArticleStatus) : ArticleFilter(status)

    data class Folders(val folder: Folder, override val status: ArticleStatus) : ArticleFilter(status)

    companion object {
        fun default() = Articles(status = ArticleStatus.ALL)
    }
}
