package com.jocmp.capy.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch

sealed class NextFilter {
    data class FolderFilter(val folderTitle: String) : NextFilter()

    data class FeedFilter(val feedID: String, val folderTitle: String? = null) : NextFilter()

    data class SearchFilter(val savedSearchID: String) : NextFilter()

    companion object {
        fun findMarkReadDestination(
            filter: ArticleFilter,
            searches: List<SavedSearch>,
            folders: List<Folder>,
            feeds: List<Feed>,
        ): NextFilter? {
            return when (filter) {
                is ArticleFilter.Feeds -> findNextFeed(filter, folders, feeds)
                is ArticleFilter.Folders -> {
                    val folderIndex = folders.indexOfFirst { it.title == filter.folderTitle }
                    val nextFolder = folders.getOrNull(folderIndex + 1)
                    val nextFeed = feeds.firstOrNull()

                    if (nextFolder != null) {
                        FolderFilter(nextFolder.title)
                    } else if (nextFeed != null) {
                        FeedFilter(feedID = nextFeed.id, folderTitle = filter.folderTitle)
                    } else {
                        null
                    }
                }

                is ArticleFilter.SavedSearches -> {
                    val index = searches.indexOfFirst { it.id == filter.savedSearchID }
                    val nextSearch = searches.getOrNull(index + 1)
                    val nextFolder = folders.firstOrNull()
                    val nextFeed = feeds.firstOrNull()

                    if (nextSearch != null) {
                        SearchFilter(nextSearch.id)
                    } else if (nextFolder != null) {
                        FolderFilter(nextFolder.title)
                    } else if (nextFeed != null) {
                        FeedFilter(feedID = nextFeed.id, folderTitle = null)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

        fun findSwipeDestination(
            filter: ArticleFilter,
            searches: List<SavedSearch>,
            feeds: List<Feed>,
            folders: List<Folder>,
        ): NextFilter? {
            return when (filter) {
                is ArticleFilter.Articles -> {
                    val firstFeed = feeds.firstOrNull()
                    val firstFolder = folders.firstOrNull()
                    val firstSearch = searches.firstOrNull()

                    if (firstSearch != null) {
                        SearchFilter(firstSearch.id)
                    } else if (firstFolder != null) {
                        FolderFilter(firstFolder.title)
                    } else if (firstFeed != null) {
                        FeedFilter(feedID = firstFeed.id, folderTitle = null)
                    } else {
                        null
                    }
                }

                is ArticleFilter.SavedSearches -> {
                    val firstFeed = feeds.firstOrNull()
                    val firstFolder = folders.firstOrNull()
                    val index = searches.indexOfFirst { filter.savedSearchID == it.id }
                    val nextSearch = searches.getOrNull(index + 1)

                    if (nextSearch != null) {
                        SearchFilter(nextSearch.id)
                    } else if (firstFolder != null) {
                        FolderFilter(firstFolder.title)
                    } else if (firstFeed != null) {
                        FeedFilter(feedID = firstFeed.id, folderTitle = null)
                    } else {
                        null
                    }
                }

                is ArticleFilter.Folders -> {
                    val firstFolderFeed = folders
                        .find { it.title == filter.folderTitle }
                        ?.feeds
                        ?.firstOrNull()

                    val nextFeed = feeds.firstOrNull()
                    val folderIndex = folders.indexOfFirst { it.title == filter.folderTitle }
                    val nextFolder = folders.getOrNull(folderIndex + 1)

                    if (firstFolderFeed != null && firstFolderFeed.folderExpanded) {
                        FeedFilter(feedID = firstFolderFeed.id, folderTitle = filter.folderTitle)
                    } else if (nextFolder != null) {
                        FolderFilter(nextFolder.title)
                    } else if (nextFeed != null) {
                        FeedFilter(feedID = nextFeed.id, folderTitle = null)
                    } else {
                        null
                    }
                }

                is ArticleFilter.Feeds -> findNextFeed(filter, folders, feeds)
            }
        }

        private fun findNextFeed(
            filter: ArticleFilter.Feeds,
            folders: List<Folder>,
            feeds: List<Feed>
        ): NextFilter? {
            return if (filter.folderTitle == null) {
                val index = feeds.indexOfFirst { it.id == filter.feedID }

                val nextFeed = feeds.getOrNull(index + 1) ?: return null

                FeedFilter(feedID = nextFeed.id, folderTitle = null)
            } else {
                val folderIndex = folders
                    .indexOfFirst { it.title == filter.folderTitle }

                val folderFeeds = folders.getOrNull(folderIndex)?.feeds.orEmpty()

                val index = folderFeeds.indexOfFirst { it.id == filter.feedID }
                val nextFolderFeed = folderFeeds.getOrNull(index + 1)
                val nextFolder = folders.getOrNull(folderIndex + 1)
                val nextFeed = feeds.firstOrNull { it.id != filter.feedID }

                if (nextFolderFeed != null) {
                    FeedFilter(feedID = nextFolderFeed.id, folderTitle = filter.folderTitle)
                } else if (nextFolder != null) {
                    FolderFilter(nextFolder.title)
                } else if (nextFeed != null) {
                    FeedFilter(feedID = nextFeed.id, folderTitle = null)
                } else {
                    null
                }
            }
        }
    }
}
