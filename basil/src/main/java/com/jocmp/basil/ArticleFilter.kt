package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
sealed class ArticleFilter(open val status: ArticleStatus) {
    fun isFolderSelect(folder: Folder): Boolean {
        return this is Folders && this.folderTitle == folder.title
    }

    fun isFeedSelected(feed: Feed): Boolean {
        return this is Feeds && this.feedID == feed.id
    }

    fun hasArticlesSelected(): Boolean {
        return this is Articles
    }

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
    data class Feeds(val feedID: String, val feedStatus: ArticleStatus) : ArticleFilter(feedStatus) {
        override val status: ArticleStatus
            get() = feedStatus
    }

    @Serializable
    data class Folders(val folderTitle: String, val folderStatus: ArticleStatus) :
        ArticleFilter(folderStatus) {
        override val status: ArticleStatus
            get() = folderStatus
    }


    companion object {
        fun default() = Articles(articleStatus = ArticleStatus.ALL)
    }
}
