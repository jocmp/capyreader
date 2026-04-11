package com.jocmp.capy.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch

class SidebarItem(
    val toFilter: (ArticleStatus) -> ArticleFilter,
    val isSelected: (ArticleFilter) -> Boolean,
    var next: SidebarItem? = null,
) {
    companion object {
        fun buildList(
            readLaterFeed: Feed? = null,
            savedSearches: List<SavedSearch> = emptyList(),
            folders: List<Folder> = emptyList(),
            feeds: List<Feed> = emptyList(),
        ): List<SidebarItem> {
            val items = mutableListOf<SidebarItem>()

            items += todayItem()
            items += articlesItem()

            if (readLaterFeed != null) {
                items += feedItem(feedID = readLaterFeed.id, folderTitle = null)
            }

            savedSearches.forEach { items += savedSearchItem(savedSearchID = it.id) }

            folders.forEach { folder ->
                items += folderItem(folderTitle = folder.title)
                if (folder.expanded) {
                    folder.feeds.forEach { feed ->
                        items += feedItem(feedID = feed.id, folderTitle = folder.title)
                    }
                }
            }

            feeds.forEach { items += feedItem(feedID = it.id, folderTitle = null) }

            items.zipWithNext().forEach { (current, next) ->
                current.next = next
            }

            return items
        }

        private fun todayItem() = SidebarItem(
            toFilter = { ArticleFilter.Today(it) },
            isSelected = { it is ArticleFilter.Today },
        )

        private fun articlesItem() = SidebarItem(
            toFilter = { ArticleFilter.Articles(articleStatus = it) },
            isSelected = { it is ArticleFilter.Articles },
        )

        private fun savedSearchItem(savedSearchID: String) = SidebarItem(
            toFilter = { ArticleFilter.SavedSearches(savedSearchID = savedSearchID, savedSearchStatus = it) },
            isSelected = { it is ArticleFilter.SavedSearches && it.savedSearchID == savedSearchID },
        )

        private fun folderItem(folderTitle: String) = SidebarItem(
            toFilter = { ArticleFilter.Folders(folderTitle = folderTitle, folderStatus = it) },
            isSelected = { it is ArticleFilter.Folders && it.folderTitle == folderTitle },
        )

        private fun feedItem(feedID: String, folderTitle: String?) = SidebarItem(
            toFilter = { ArticleFilter.Feeds(feedID = feedID, folderTitle = folderTitle, feedStatus = it) },
            isSelected = {
                it is ArticleFilter.Feeds &&
                        it.feedID == feedID &&
                        it.folderTitle.orEmpty() == folderTitle.orEmpty()
            },
        )
    }
}
