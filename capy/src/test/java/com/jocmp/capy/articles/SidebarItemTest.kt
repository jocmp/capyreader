package com.jocmp.capy.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Folder
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.repeated
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SidebarItemTest {
    private lateinit var feedFixture: FeedFixture

    @BeforeTest
    fun setup() {
        val database = InMemoryDatabaseProvider.build(accountID = "1009")
        feedFixture = FeedFixture(database)
    }

    private fun findNext(
        filter: ArticleFilter,
        searches: List<SavedSearch> = emptyList(),
        folders: List<Folder> = emptyList(),
        feeds: List<com.jocmp.capy.Feed> = emptyList(),
    ): SidebarItem? {
        val items = SidebarItem.buildList(
            savedSearches = searches,
            folders = folders,
            feeds = feeds,
        )
        return items.find { it.isSelected(filter) }?.next
    }

    @Test
    fun `next from today with a saved search`() {
        val filter = ArticleFilter.Today(ArticleStatus.ALL)
        val search = SavedSearch(id = "1", name = "My Search", query = null)

        val next = findNext(
            filter,
            folders = listOf(Folder(title = "Uncategorized")),
            searches = listOf(search),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.SavedSearches>(nextFilter)
        assertEquals(expected = search.id, actual = nextFilter.savedSearchID)
    }

    @Test
    fun `next from last saved search to first folder`() {
        val folder = Folder(title = "This Is My Next Folder")
        val search = SavedSearch(id = "2", name = "My Second Search", query = null)
        val filter = ArticleFilter.SavedSearches(
            savedSearchID = search.id,
            savedSearchStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            folders = listOf(folder),
            searches = listOf(search),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Folders>(nextFilter)
        assertEquals(expected = folder.title, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from today with a folder`() {
        val filter = ArticleFilter.Today(ArticleStatus.ALL)
        val folder = Folder(title = "This Is My Next Folder")

        val next = findNext(
            filter,
            folders = listOf(folder),
        )

        assertNotNull(next)
        assertIs<ArticleFilter.Folders>(next.toFilter(ArticleStatus.ALL))
    }

    @Test
    fun `next from today with a feed`() {
        val filter = ArticleFilter.Today(ArticleStatus.ALL)
        val feed = feedFixture.create()

        val next = findNext(
            filter,
            feeds = listOf(feed),
        )

        assertNotNull(next)
        assertIs<ArticleFilter.Feeds>(next.toFilter(ArticleStatus.ALL))
    }

    @Test
    fun `next from today that is empty`() {
        val filter = ArticleFilter.Today(ArticleStatus.ALL)

        val next = findNext(filter)

        assertNull(next)
    }

    @Test
    fun `next from today with a named folder`() {
        val filter = ArticleFilter.Today(ArticleStatus.ALL)
        val folder = Folder(title = "My Folder")

        val next = findNext(
            filter,
            folders = listOf(folder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Folders>(nextFilter)
        assertEquals(expected = folder.title, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from expanded folder to first folder feed`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title").copy(folderExpanded = true)
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds,
            expanded = true,
        )
        val anotherFolder = Folder(title = "Bad folder")
        val filter = ArticleFilter.Folders(
            folderTitle = folder.title,
            folderStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            folders = listOf(folder, anotherFolder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Feeds>(nextFilter)
        assertEquals(expected = folderFeeds.first().id, actual = nextFilter.feedID)
        assertEquals(expected = folder.title, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from collapsed folder skips feeds to next folder`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds,
            expanded = false,
        )
        val anotherFolder = Folder(title = "Next folder")
        val filter = ArticleFilter.Folders(
            folderTitle = folder.title,
            folderStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            folders = listOf(folder, anotherFolder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Folders>(nextFilter)
        assertEquals(expected = anotherFolder.title, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from collapsed folder with no sibling returns null`() {
        val folderTitle = "My Folder"
        val folder = Folder(title = folderTitle)

        val filter = ArticleFilter.Folders(
            folderTitle = folder.title,
            folderStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(filter, folders = listOf(folder))

        assertNull(next)
    }

    @Test
    fun `next from top-level feed to next top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }

        val filter = ArticleFilter.Feeds(
            feedID = topLevelFeeds.first().id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            feeds = topLevelFeeds,
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Feeds>(nextFilter)
        assertEquals(expected = topLevelFeeds[1].id, actual = nextFilter.feedID)
        assertNull(nextFilter.folderTitle)
    }

    @Test
    fun `next from last top-level feed is null`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }

        val filter = ArticleFilter.Feeds(
            feedID = topLevelFeeds[2].id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            feeds = topLevelFeeds,
        )

        assertNull(next)
    }

    @Test
    fun `next from folder feed to next folder feed`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds,
            expanded = true,
        )
        val anotherFolder = Folder(title = "Next Folder")
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds.first().id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            folders = listOf(folder, anotherFolder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Feeds>(nextFilter)
        assertEquals(expected = folderFeeds[1].id, actual = nextFilter.feedID)
        assertEquals(expected = folderTitle, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from last folder feed to next folder`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds,
            expanded = true,
        )
        val anotherFolder = Folder(title = "Next Folder")
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            folders = listOf(folder, anotherFolder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Folders>(nextFilter)
        assertEquals(expected = anotherFolder.title, actual = nextFilter.folderTitle)
    }

    @Test
    fun `next from last folder feed to first top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Top Level Title")
        }
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My nested title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds,
            expanded = true,
        )
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD,
        )

        val next = findNext(
            filter,
            feeds = topLevelFeeds,
            folders = listOf(folder),
        )

        assertNotNull(next)
        val nextFilter = next.toFilter(ArticleStatus.ALL)
        assertIs<ArticleFilter.Feeds>(nextFilter)
        assertEquals(expected = topLevelFeeds.first().id, actual = nextFilter.feedID)
        assertNull(nextFilter.folderTitle)
    }

    @Test
    fun `buildList ordering matches sidebar`() {
        val search = SavedSearch(id = "1", name = "Search", query = null)
        val folderFeed = feedFixture.create(title = "Folder Feed")
        val folder = Folder(title = "My Folder", feeds = listOf(folderFeed), expanded = true)
        val topLevelFeed = feedFixture.create(title = "Top Level")

        val items = SidebarItem.buildList(
            savedSearches = listOf(search),
            folders = listOf(folder),
            feeds = listOf(topLevelFeed),
        )

        val filters = items.map { it.toFilter(ArticleStatus.ALL) }
        assertIs<ArticleFilter.Articles>(filters[0])
        assertIs<ArticleFilter.Today>(filters[1])
        assertIs<ArticleFilter.SavedSearches>(filters[2])
        assertIs<ArticleFilter.Folders>(filters[3])
        val folderFeedFilter = filters[4]
        assertIs<ArticleFilter.Feeds>(folderFeedFilter)
        assertEquals(expected = folder.title, actual = folderFeedFilter.folderTitle)
        val topLevelFilter = filters[5]
        assertIs<ArticleFilter.Feeds>(topLevelFilter)
        assertNull(topLevelFilter.folderTitle)
    }

    @Test
    fun `linked list is wired correctly`() {
        val feed = feedFixture.create()
        val items = SidebarItem.buildList(feeds = listOf(feed))

        val articles = items[0]
        assertTrue(articles.isSelected(ArticleFilter.Articles(ArticleStatus.ALL)))
        assertNotNull(articles.next)
        assertTrue(articles.next!!.isSelected(ArticleFilter.Today(ArticleStatus.ALL)))
        assertNotNull(articles.next?.next)
        assertTrue(articles.next!!.next!!.isSelected(
            ArticleFilter.Feeds(feedID = feed.id, folderTitle = null, feedStatus = ArticleStatus.ALL)
        ))
        assertNull(articles.next?.next?.next)
    }
}
