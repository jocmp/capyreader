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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NextFilterTest {
    private lateinit var feedFixture: FeedFixture

    @BeforeTest
    fun setup() {
        val database = InMemoryDatabaseProvider.build(accountID = "1009")
        feedFixture = FeedFixture(database)
    }

    @Test
    fun `findSwipeDestination on article filter with a saved search`() {
        val filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
        val search = SavedSearch(id = "1", name = "My Search", query = null)

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = emptyList(),
            folders = listOf(Folder(title = "Uncategorized")),
            searches = listOf(search)
        )!!

        assertTrue(next is NextFilter.SearchFilter)
        assertEquals(actual = next.savedSearchID, expected = search.id)
    }

    @Test
    fun `findSwipeDestination on article filter with the last saved search`() {
        val folder = Folder(title = "This Is My Next Folder")
        val folders = listOf(folder)
        val search = SavedSearch(id = "2", name = "My Second Search", query = null)
        val searches = listOf(search)
        val filter = ArticleFilter.SavedSearches(
            savedSearchID = search.id,
            savedSearchStatus = ArticleStatus.UNREAD
        )

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = emptyList(),
            folders = folders,
            searches = searches,
        )!!

        assertTrue(next is NextFilter.FolderFilter)
        assertEquals(actual = next.folderTitle, expected = folder.title)
    }

    @Test
    fun `findSwipeDestination on article filter with a folder`() {
        val filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
        val folder = Folder(title = "This Is My Next Folder")
        val folders = listOf(folder)

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = emptyList(),
            folders = folders,
            searches = emptyList(),
        )!!

        assertTrue(next is NextFilter.FolderFilter)
        assertEquals(actual = next.folderTitle, expected = folder.title)
    }

    @Test
    fun `findSwipeDestination on article filter with a feed`() {
        val filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
        val feed = feedFixture.create()
        val feeds = listOf(feed)

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = feeds,
            folders = emptyList(),
            searches = emptyList(),
        )!!

        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = feed.id)
        assertNull(next.folderTitle)
    }

    @Test
    fun `findSwipeDestination on article filter that is empty`() {
        val filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = emptyList(),
            folders = emptyList(),
            searches = emptyList()
        )

        assertNull(next)
    }

    @Test
    fun `findSwipeDestination on a folder filter with a feed`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val anotherFolder = Folder(title = "Bad folder")
        val filter = ArticleFilter.Folders(
            folderTitle = folder.title,
            folderStatus = ArticleStatus.UNREAD
        )

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = emptyList(),
            folders = listOf(anotherFolder, folder),
            searches = emptyList(),
        )!!

        val expectedFeed = folderFeeds.first()
        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = expectedFeed.id)
        assertEquals(actual = next.folderTitle, expected = folder.title)
    }

    @Test
    fun `findSwipeDestination on a folder filter that is empty`() {
        val folderTitle = "My Folder"
        val folder = Folder(title = folderTitle)
        val anotherFolder = Folder(title = "Bad folder")

        val filter = ArticleFilter.Folders(
            folderTitle = folder.title,
            folderStatus = ArticleStatus.UNREAD
        )

        val next =
            NextFilter.findSwipeDestination(
                filter,
                feeds = emptyList(),
                folders = listOf(anotherFolder, folder),
                searches = emptyList(),
            )

        assertNull(next)
    }

    @Test
    fun `findSwipeDestination on a feed filter that is a top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val someFolder = Folder(title = "Some folder")
        val anotherFolder = Folder(title = "Yet another folder")

        val filter = ArticleFilter.Feeds(
            feedID = topLevelFeeds.first().id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = topLevelFeeds,
            folders = listOf(someFolder, anotherFolder),
            searches = emptyList(),
        )!!

        val expectedFeed = topLevelFeeds[1]
        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = expectedFeed.id)
        assertNull(next.folderTitle)
    }

    @Test
    fun `findSwipeDestination on a feed filter that is the last top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val someFolder = Folder(title = "Some folder")
        val anotherFolder = Folder(title = "Yet another folder")

        val filter = ArticleFilter.Feeds(
            feedID = topLevelFeeds[2].id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )

        val next = NextFilter.findSwipeDestination(
            filter,
            feeds = topLevelFeeds,
            folders = listOf(someFolder, anotherFolder),
            searches = emptyList(),
        )

        assertNull(next)
    }

    @Test
    fun `findSwipeDestination on the last folder feed with a next feed`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val anotherFolder = Folder(title = "Next Folder")
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds.first().id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD
        )
        val next =
            NextFilter.findSwipeDestination(
                filter,
                feeds = emptyList(),
                folders = listOf(folder, anotherFolder),
                searches = emptyList(),
            )!!

        val expectedFeed = folderFeeds[1]
        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = expectedFeed.id)
        assertEquals(actual = next.folderTitle, expected = folderTitle)
    }

    @Test
    fun `findSwipeDestination on the last folder feed with a next folder`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val anotherFolder = Folder(title = "Next Folder")
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD
        )
        val next =
            NextFilter.findSwipeDestination(
                filter,
                feeds = emptyList(),
                folders = listOf(folder, anotherFolder),
                searches = emptyList(),
            )!!

        assertTrue(next is NextFilter.FolderFilter)
        assertEquals(actual = next.folderTitle, expected = anotherFolder.title)
    }

    @Test
    fun `findSwipeDestination on the last folder feed with a next top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Top Level Title")
        }
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My nested title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD
        )
        val next =
            NextFilter.findSwipeDestination(
                filter,
                feeds = topLevelFeeds,
                folders = listOf(folder),
                searches = emptyList(),
            )!!

        val expectedFeed = topLevelFeeds.first()
        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = expectedFeed.id)
        assertNull(next.folderTitle)
    }

    @Test
    fun `findMarkReadDestination on the last search filter and a next folder`() {
        val someFolder = Folder(title = "Some folder")
        val search = SavedSearch(id = "1", name = "My Search", query = null)
        val filter = ArticleFilter.SavedSearches(search.id, savedSearchStatus = ArticleStatus.UNREAD)

        val next = NextFilter.findMarkReadDestination(
            filter,
            searches = listOf(search),
            feeds = emptyList(),
            folders = listOf(someFolder)
        )

        assertEquals(expected = NextFilter.FolderFilter(someFolder.title), actual = next)
    }


    @Test
    fun `findMarkReadDestination on the last search filter and a next feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val nextFeed = topLevelFeeds.first()
        val search = SavedSearch(id = "1", name = "My Search", query = null)
        val filter = ArticleFilter.SavedSearches(search.id, savedSearchStatus = ArticleStatus.UNREAD)

        val next = NextFilter.findMarkReadDestination(
            filter,
            searches = listOf(search),
            feeds = topLevelFeeds,
            folders = emptyList()
        )

        assertEquals(expected = NextFilter.FeedFilter(feedID = nextFeed.id), actual = next)
    }

    @Test
    fun `findMarkReadDestination on a feed filter that is the last top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val someFolder = Folder(title = "Some folder")
        val anotherFolder = Folder(title = "Yet another folder")

        val filter = ArticleFilter.Feeds(
            feedID = topLevelFeeds[2].id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )

        val next = NextFilter.findMarkReadDestination(
            filter,
            searches = emptyList(),
            feeds = topLevelFeeds,
            folders = listOf(someFolder, anotherFolder)
        )

        assertNull(next)
    }

    @Test
    fun `findMarkReadDestination on the last folder feed with a next folder`() {
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My Title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val anotherFolder = Folder(title = "Next Folder")
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD
        )
        val next =
            NextFilter.findMarkReadDestination(
                filter,
                searches = emptyList(),
                feeds = emptyList(),
                folders = listOf(folder, anotherFolder)
            )!!

        assertTrue(next is NextFilter.FolderFilter)
        assertEquals(actual = next.folderTitle, expected = anotherFolder.title)
    }

    @Test
    fun `findMarkReadDestination on the last folder feed with a next top-level feed`() {
        val topLevelFeeds = 3.repeated { index ->
            feedFixture.create(title = "${index + 1} My Top Level Title")
        }
        val folderTitle = "My Folder"
        val folderFeeds = 2.repeated { index ->
            feedFixture.create(title = "${index + 1} My nested title")
        }
        val folder = Folder(
            title = folderTitle,
            feeds = folderFeeds
        )
        val filter = ArticleFilter.Feeds(
            feedID = folderFeeds[1].id,
            folderTitle = folderTitle,
            feedStatus = ArticleStatus.UNREAD
        )
        val next = NextFilter.findMarkReadDestination(
            filter,
            searches = emptyList(),
            feeds = topLevelFeeds,
            folders = listOf(folder)
        )!!

        val expectedFeed = topLevelFeeds.first()
        assertTrue(next is NextFilter.FeedFilter)
        assertEquals(actual = next.feedID, expected = expectedFeed.id)
        assertNull(next.folderTitle)
    }
}
