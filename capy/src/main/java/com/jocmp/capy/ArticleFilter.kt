package com.jocmp.capy

import kotlinx.serialization.Serializable

@Serializable
sealed class ArticleFilter(open val status: ArticleStatus) {
    fun isFolderSelected(folder: Folder): Boolean {
        return this is Folders && this.folderTitle == folder.title
    }

    fun isFeedSelected(feed: Feed): Boolean {
        return this is Feeds && this.feedID == feed.id && this.folderTitle.orEmpty() == feed.folderName
    }

    fun isSavedSearchSelected(savedSearch: SavedSearch): Boolean {
        return this is SavedSearches && this.savedSearchID == savedSearch.id
    }

    fun hasUnreadSelected(): Boolean {
        return this is Unread
    }

    fun hasTodaySelected(): Boolean {
        return this is Today
    }

    fun hasStarredSelected(): Boolean {
        return this is Starred
    }

    fun withStatus(status: ArticleStatus): ArticleFilter {
        return when (this) {
            is Unread -> this
            is Feeds -> copy(feedStatus = status)
            is Folders -> copy(folderStatus = status)
            is SavedSearches -> copy(savedSearchStatus = status)
            is Today -> copy(todayStatus = status)
            is Starred -> this
        }
    }

    @Serializable object Unread : ArticleFilter(ArticleStatus.UNREAD)

    @Serializable
    data class Feeds(val feedID: String, val folderTitle: String?, val feedStatus: ArticleStatus) :
        ArticleFilter(feedStatus) {
        override val status: ArticleStatus
            get() = feedStatus
    }

    @Serializable
    data class Folders(val folderTitle: String, val folderStatus: ArticleStatus) :
        ArticleFilter(folderStatus) {
        override val status: ArticleStatus
            get() = folderStatus
    }

    @Serializable
    data class SavedSearches(
        val savedSearchID: String,
        val savedSearchStatus: ArticleStatus
    ) : ArticleFilter(savedSearchStatus) {
        override val status: ArticleStatus
            get() = savedSearchStatus
    }

    @Serializable
    data class Today(val todayStatus: ArticleStatus) : ArticleFilter(todayStatus) {
        override val status: ArticleStatus
            get() = todayStatus
    }

    @Serializable
    object Starred : ArticleFilter(ArticleStatus.ALL)

    companion object {
        fun default() = Unread
    }
}
