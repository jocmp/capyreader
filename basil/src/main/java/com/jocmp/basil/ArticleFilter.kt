package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
sealed class ArticleFilter(open val status: ArticleStatus) {
    fun withStatus(status: ArticleStatus): ArticleFilter {
        return when (this) {
            is Articles -> copy(articleStatus = status)
            is Feeds -> copy(feedStatus = status)
            is Folders -> copy(folderStatus = status)
        }
    }

    @Serializable
    data class Articles(val articleStatus: ArticleStatus) : ArticleFilter(articleStatus) {
        override val status: ArticleStatus
            get() = articleStatus
    }

    @Serializable
    data class Feeds(val feed: Feed, val feedStatus: ArticleStatus) : ArticleFilter(feedStatus) {
        override val status: ArticleStatus
            get() = feedStatus
    }

    @Serializable
    data class Folders(val folder: Folder, val folderStatus: ArticleStatus) :
        ArticleFilter(folderStatus) {
        override val status: ArticleStatus
            get() = folderStatus
    }


    companion object {
        fun default() = Articles(articleStatus = ArticleStatus.ALL)
    }
}
