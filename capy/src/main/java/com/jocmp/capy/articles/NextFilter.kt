package com.jocmp.capy.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder

sealed class NextFilter {
    data class FolderFilter(val folderTitle: String) : NextFilter()

    data class FeedFilter(val feedID: String, val folderTitle: String? = null) : NextFilter()

    companion object {
        fun find(
            filter: ArticleFilter,
            feeds: List<Feed>,
            folders: List<Folder>,
        ): NextFilter? {
            return when (filter) {
                is ArticleFilter.Articles -> {
                    val firstFeed = feeds.firstOrNull()
                    val firstFolder = folders.firstOrNull()

                    if (firstFolder != null) {
                        FolderFilter(firstFolder.title)
                    } else if (firstFeed != null) {
                        FeedFilter(feedID = firstFeed.id, folderTitle = null)
                    } else {
                        null
                    }
                }

                is ArticleFilter.Folders -> {
                    val firstFeed = folders
                        .find { it.title == filter.folderTitle }
                        ?.feeds
                        ?.firstOrNull() ?: return null

                    FeedFilter(feedID = firstFeed.id, folderTitle = filter.folderTitle)
                }

                is ArticleFilter.Feeds -> {
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
                        val nextFeed = feeds.firstOrNull()

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
    }
}
