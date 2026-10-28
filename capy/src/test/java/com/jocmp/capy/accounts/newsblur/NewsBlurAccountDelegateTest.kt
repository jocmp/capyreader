package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.InMemoryDataStore
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.newsblurclient.AddURLResponse
import com.jocmp.newsblurclient.CodeResponse
import com.jocmp.newsblurclient.Feed
import com.jocmp.newsblurclient.FeedsResponse
import com.jocmp.newsblurclient.NewsBlur
import com.jocmp.newsblurclient.StarredStoryHashesResponse
import com.jocmp.newsblurclient.StoriesResponse
import com.jocmp.newsblurclient.Story
import com.jocmp.newsblurclient.UnreadStoryHashesResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewsBlurAccountDelegateTest {
    private val accountID = "777"
    private lateinit var database: Database
    private lateinit var newsBlur: NewsBlur
    private lateinit var feedFixture: FeedFixture
    private lateinit var delegate: AccountDelegate

    private val googleFeed = Feed(
        id = 9451748,
        feed_title = "9to5Google",
        feed_address = "https://9to5google.com/feed/",
        feed_link = "https://9to5google.com/",
        favicon_url = "https://s3.amazonaws.com/icons.newsblur.com/9451748.png",
    )

    private val vergeFeed = Feed(
        id = 5,
        feed_title = "The Verge",
        feed_address = "https://www.theverge.com/rss/index.xml",
        feed_link = "https://www.theverge.com",
    )

    private val feedsResponse = FeedsResponse(
        feeds = mapOf(
            googleFeed.id.toString() to googleFeed,
            vergeFeed.id.toString() to vergeFeed,
        ),
        flat_folders = mapOf(
            " " to listOf(vergeFeed.id),
            "Tech" to listOf(googleFeed.id),
        ),
    )

    private val unreadStory = Story(
        story_hash = "9451748:a6f371",
        story_feed_id = 9451748,
        story_title = "The Samsung Galaxy S25 Ultra is at its lowest price for a while",
        story_content = "<p>Deal</p>",
        story_permalink = "https://9to5google.com/2026/10/28/galaxy-s25-ultra-deal/",
        story_authors = "Damien Wilde",
        story_timestamp = nowUTC().toEpochSecond().toString(),
        image_urls = listOf("https://9to5google.com/image.jpg"),
        read_status = 0,
    )

    private val starredStory = Story(
        story_hash = "5:b1c2d3",
        story_feed_id = 5,
        story_title = "A starred story",
        story_content = "<p>Starred</p>",
        story_permalink = "https://www.theverge.com/starred",
        story_authors = "",
        story_timestamp = nowUTC().toEpochSecond().toString(),
        read_status = 1,
        starred = true,
    )

    @BeforeTest
    fun setup() {
        mockkObject(CapyLog)
        every { CapyLog.warn(any(), any()) }.returns(Unit)
        every { CapyLog.info(any(), any()) }.returns(Unit)
        every { CapyLog.error(any(), any(), any()) }.returns(Unit)

        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        newsBlur = mockk()
        delegate = NewsBlurAccountDelegate(database, newsBlur, AccountPreferences(InMemoryDataStore()))
    }

    @Test
    fun refresh_savesFeedsAndStories() = runTest {
        stubRefresh(stories = listOf(unreadStory, starredStory))

        val result = delegate.refresh(ArticleFilter.default())

        assertTrue(result.isSuccess)

        val feeds = database.feedsQueries.all().executeAsList()
        assertEquals(2, feeds.size)

        val taggedNames = database.feedsQueries.tagged().executeAsList().map { it.name }
        assertEquals(listOf(null, "Tech"), taggedNames.sortedWith(nullsFirst(naturalOrder())))

        val articles = ArticleRecords(database)
        val unread = articles.find(unreadStory.story_hash)!!
        assertFalse(unread.read)
        assertFalse(unread.starred)
        assertEquals("Damien Wilde", unread.author)
        assertEquals("https://9to5google.com/image.jpg", unread.imageURL)

        val starred = articles.find(starredStory.story_hash)!!
        assertTrue(starred.read)
        assertTrue(starred.starred)
        assertNull(starred.author)

        coVerify {
            newsBlur.riverStories(storyHashes = match { it.toSet() == setOf(unreadStory.story_hash, starredStory.story_hash) })
        }
    }

    @Test
    fun refresh_removesUnsubscribedFeeds() = runTest {
        feedFixture.create(feedID = "123", title = "Removed")
        stubRefresh(stories = emptyList())

        delegate.refresh(ArticleFilter.default())

        val feedIDs = database.feedsQueries.all().executeAsList().map { it.id }
        assertEquals(setOf("5", "9451748"), feedIDs.toSet())
    }

    @Test
    fun refresh_chunksStoryHashes() = runTest {
        val hashes = (1..150).map { "9451748:$it" }

        stubRefresh(stories = emptyList(), unreadHashes = hashes, starredHashes = emptyList())

        delegate.refresh(ArticleFilter.default())

        coVerify(exactly = 1) { newsBlur.riverStories(storyHashes = match { it.size == 100 }) }
        coVerify(exactly = 1) { newsBlur.riverStories(storyHashes = match { it.size == 50 }) }
    }

    @Test
    fun refresh_skipsArticlesPastAutoDeleteCutoff() = runTest {
        val oldReadStory = unreadStory.copy(
            story_hash = "9451748:old",
            story_timestamp = nowUTC().minusMonths(3).toEpochSecond().toString(),
            read_status = 1,
        )

        stubRefresh(
            stories = listOf(unreadStory, oldReadStory),
            unreadHashes = listOf(unreadStory.story_hash, oldReadStory.story_hash),
            starredHashes = emptyList(),
        )

        delegate.refresh(ArticleFilter.default(), cutoffDate = nowUTC().minusMonths(1))

        val articles = ArticleRecords(database)
        assertTrue(articles.find(unreadStory.story_hash) != null)
        assertNull(articles.find(oldReadStory.story_hash))
    }

    @Test
    fun refresh_unauthenticatedHashes() = runTest {
        stubRefresh(stories = listOf(unreadStory))
        coEvery { newsBlur.unreadStoryHashes() }.returns(
            Response.success(
                UnreadStoryHashesResponse(
                    unread_feed_story_hashes = mapOf("1" to listOf("1:demo")),
                    authenticated = false,
                )
            )
        )

        val result = delegate.refresh(ArticleFilter.default())

        assertIs<UnauthorizedError>(result.exceptionOrNull())
        assertNull(ArticleRecords(database).find("1:demo"))
    }

    @Test
    fun refresh_forbiddenFeeds() = runTest {
        coEvery { newsBlur.feeds() }.returns(forbidden())

        val result = delegate.refresh(ArticleFilter.default())

        assertIs<UnauthorizedError>(result.exceptionOrNull())
    }

    @Test
    fun refresh_IOException() = runTest {
        val networkError = SocketTimeoutException("Network timeout")
        coEvery { newsBlur.feeds() }.throws(networkError)

        val result = delegate.refresh(ArticleFilter.default())

        assertEquals(networkError, result.exceptionOrNull())
    }

    @Test
    fun markRead() = runTest {
        val ids = (1..60).map { "1:$it" }
        coEvery { newsBlur.markStoryHashesAsRead(any()) }.returns(success())

        val result = delegate.markRead(ids)

        assertTrue(result.isSuccess)
        coVerify { newsBlur.markStoryHashesAsRead(ids.take(50)) }
        coVerify { newsBlur.markStoryHashesAsRead(ids.drop(50)) }
    }

    @Test
    fun markUnread() = runTest {
        coEvery { newsBlur.markStoryHashAsUnread(any()) }.returns(success())

        delegate.markUnread(listOf("1:a"))

        coVerify { newsBlur.markStoryHashAsUnread(listOf("1:a")) }
    }

    @Test
    fun addStar() = runTest {
        coEvery { newsBlur.markStoryHashAsStarred(any()) }.returns(success())

        delegate.addStar(listOf("1:a"))

        coVerify { newsBlur.markStoryHashAsStarred(listOf("1:a")) }
    }

    @Test
    fun removeStar() = runTest {
        coEvery { newsBlur.markStoryHashAsUnstarred(any()) }.returns(success())

        delegate.removeStar(listOf("1:a"))

        coVerify { newsBlur.markStoryHashAsUnstarred(listOf("1:a")) }
    }

    @Test
    fun markRead_forbidden() = runTest {
        coEvery { newsBlur.markStoryHashesAsRead(any()) }.returns(forbidden())

        val result = delegate.markRead(listOf("1:a"))

        assertIs<UnauthorizedError>(result.exceptionOrNull())
    }

    @Test
    fun addFeed() = runTest {
        coEvery { newsBlur.addFolder("Tech") }.returns(success())
        coEvery { newsBlur.addURL(url = googleFeed.feed_address, folder = "Tech") }.returns(
            Response.success(AddURLResponse(code = 1, feed = googleFeed))
        )
        stubArticleState()

        val result = delegate.addFeed(
            url = googleFeed.feed_address,
            title = null,
            folderTitles = listOf("Tech")
        )

        val feed = (result as AddFeedResult.Success).feed
        assertEquals("9to5Google", feed.title)
        assertEquals(listOf("Tech"), folderNames(feed.id))
        coVerify(exactly = 0) { newsBlur.renameFeed(any(), any()) }
    }

    @Test
    fun addFeed_withTitleAndFolders() = runTest {
        coEvery { newsBlur.addFolder(any()) }.returns(success())
        coEvery { newsBlur.addURL(url = googleFeed.feed_address, folder = "Tech") }.returns(
            Response.success(AddURLResponse(code = 1, feed = googleFeed))
        )
        coEvery { newsBlur.moveFeedToFolders(any(), any(), any()) }.returns(success())
        coEvery { newsBlur.renameFeed(any(), any()) }.returns(success())
        stubArticleState()

        val result = delegate.addFeed(
            url = googleFeed.feed_address,
            title = "Google News",
            folderTitles = listOf("Tech", "Android")
        )

        val feed = (result as AddFeedResult.Success).feed
        assertEquals("Google News", feed.title)

        coVerify { newsBlur.addFolder("Android") }
        coVerify {
            newsBlur.moveFeedToFolders(
                feedID = googleFeed.id,
                inFolders = listOf("Tech"),
                toFolders = listOf("Tech", "Android"),
            )
        }
        coVerify { newsBlur.renameFeed(feedID = googleFeed.id, title = "Google News") }
    }

    @Test
    fun addFeed_notFound() = runTest {
        coEvery { newsBlur.addURL(url = any(), folder = "") }.returns(
            Response.success(AddURLResponse(code = -1, message = "This address does not point to an RSS feed."))
        )

        val result = delegate.addFeed(url = "https://example.com", title = null, folderTitles = null)

        assertIs<AddFeedResult.Error.FeedNotFound>((result as AddFeedResult.Failure).error)
    }

    @Test
    fun addFeed_networkError() = runTest {
        coEvery { newsBlur.addURL(url = any(), folder = any()) }.throws(SocketTimeoutException())

        val result = delegate.addFeed(url = "https://example.com", title = null, folderTitles = null)

        assertIs<AddFeedResult.Error.NetworkError>((result as AddFeedResult.Failure).error)
    }

    @Test
    fun addFeed_unauthorized() = runTest {
        coEvery { newsBlur.addURL(url = any(), folder = any()) }.returns(forbidden())

        val result = delegate.addFeed(url = "https://example.com", title = null, folderTitles = null)

        assertIs<AddFeedResult.Error.ConnectionError>((result as AddFeedResult.Failure).error)
    }

    @Test
    fun addFeed_succeedsWhenArticleSyncFails() = runTest {
        coEvery { newsBlur.addURL(url = googleFeed.feed_address, folder = "") }.returns(
            Response.success(AddURLResponse(code = 1, feed = googleFeed))
        )
        coEvery { newsBlur.unreadStoryHashes() }.returns(Response.error(502, "".toResponseBody()))

        val result = delegate.addFeed(url = googleFeed.feed_address, title = null, folderTitles = null)

        assertIs<AddFeedResult.Success>(result)
    }

    @Test
    fun updateFeed_movesFolders() = runTest {
        val feed = feedFixture.create(feedID = googleFeed.id.toString(), title = "9to5Google", folderNames = listOf("Tech"))

        coEvery { newsBlur.feeds() }.returns(Response.success(feedsResponse))
        coEvery { newsBlur.addFolder(any()) }.returns(success())
        coEvery { newsBlur.moveFeedToFolders(any(), any(), any()) }.returns(success())
        coEvery { newsBlur.renameFeed(any(), any()) }.returns(success())

        val result = delegate.updateFeed(feed = feed, title = "Google", folderTitles = listOf("Android"))

        assertEquals("Google", result.getOrNull()!!.title)
        assertEquals(listOf("Android"), folderNames(feed.id))
        coVerify { newsBlur.renameFeed(feedID = googleFeed.id, title = "Google") }
        coVerify {
            newsBlur.moveFeedToFolders(
                feedID = googleFeed.id,
                inFolders = listOf("Tech"),
                toFolders = listOf("Android"),
            )
        }
    }

    @Test
    fun updateFeed_toTopLevel() = runTest {
        val feed = feedFixture.create(feedID = googleFeed.id.toString(), title = "9to5Google", folderNames = listOf("Tech"))

        coEvery { newsBlur.feeds() }.returns(Response.success(feedsResponse))
        coEvery { newsBlur.moveFeedToFolders(any(), any(), any()) }.returns(success())

        val result = delegate.updateFeed(feed = feed, title = "9to5Google", folderTitles = emptyList())

        assertEquals(emptyList(), folderNames(feed.id))
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { newsBlur.renameFeed(any(), any()) }
        coVerify {
            newsBlur.moveFeedToFolders(
                feedID = googleFeed.id,
                inFolders = listOf("Tech"),
                toFolders = listOf(""),
            )
        }
    }

    @Test
    fun updateFeed_unchangedFolders() = runTest {
        val feed = feedFixture.create(feedID = googleFeed.id.toString(), title = "9to5Google", folderNames = listOf("Tech"))

        coEvery { newsBlur.feeds() }.returns(Response.success(feedsResponse))

        delegate.updateFeed(feed = feed, title = "9to5Google", folderTitles = listOf("Tech"))

        coVerify(exactly = 0) { newsBlur.moveFeedToFolders(any(), any(), any()) }
    }

    @Test
    fun updateFolder() = runTest {
        coEvery { newsBlur.renameFolder(any(), any(), any()) }.returns(success())

        val result = delegate.updateFolder(oldTitle = "Tech", newTitle = "Technology")

        assertTrue(result.isSuccess)
        coVerify { newsBlur.renameFolder(folder = "Tech", newFolderName = "Technology", inFolder = "") }
    }

    @Test
    fun removeFeed_deletesEachPlacement() = runTest {
        val feed = feedFixture.create(feedID = googleFeed.id.toString())

        coEvery { newsBlur.feeds() }.returns(
            Response.success(
                feedsResponse.copy(
                    flat_folders = mapOf(" " to listOf(googleFeed.id), "Tech" to listOf(googleFeed.id))
                )
            )
        )
        coEvery { newsBlur.deleteFeed(any(), any()) }.returns(success())

        val result = delegate.removeFeed(feed)

        assertTrue(result.isSuccess)
        coVerify { newsBlur.deleteFeed(feedID = googleFeed.id, inFolder = "") }
        coVerify { newsBlur.deleteFeed(feedID = googleFeed.id, inFolder = "Tech") }
    }

    @Test
    fun removeFolder_keepsFeeds() = runTest {
        coEvery { newsBlur.feeds() }.returns(
            Response.success(
                feedsResponse.copy(
                    flat_folders = mapOf(
                        " " to emptyList(),
                        "Tech" to listOf(googleFeed.id, vergeFeed.id),
                        "News" to listOf(vergeFeed.id),
                    )
                )
            )
        )
        coEvery { newsBlur.moveFeedToFolders(any(), any(), any()) }.returns(success())
        coEvery { newsBlur.deleteFolder(any(), any()) }.returns(success())

        val result = delegate.removeFolder("Tech")

        assertTrue(result.isSuccess)
        coVerify {
            newsBlur.moveFeedToFolders(feedID = googleFeed.id, inFolders = listOf("Tech"), toFolders = listOf(""))
        }
        coVerify {
            newsBlur.moveFeedToFolders(feedID = vergeFeed.id, inFolders = listOf("Tech"), toFolders = emptyList())
        }
        coVerify { newsBlur.deleteFolder(folder = "Tech", inFolder = "") }
    }

    @Test
    fun removeFolder_movesNestedFeeds() = runTest {
        coEvery { newsBlur.feeds() }.returns(
            Response.success(
                feedsResponse.copy(
                    flat_folders = mapOf(
                        " " to emptyList(),
                        "Tech" to listOf(googleFeed.id),
                        "Tech - Rust" to listOf(vergeFeed.id),
                    )
                )
            )
        )
        coEvery { newsBlur.moveFeedToFolders(any(), any(), any()) }.returns(success())
        coEvery { newsBlur.deleteFolder(any(), any()) }.returns(success())

        delegate.removeFolder("Tech")

        coVerify {
            newsBlur.moveFeedToFolders(feedID = vergeFeed.id, inFolders = listOf("Rust"), toFolders = listOf(""))
        }
        coVerify { newsBlur.deleteFolder(folder = "Tech", inFolder = "") }
    }

    @Test
    fun updateFolder_forbidden() = runTest {
        coEvery { newsBlur.renameFolder(any(), any(), any()) }.returns(forbidden())

        val result = delegate.updateFolder(oldTitle = "Tech", newTitle = "Technology")

        assertIs<UnauthorizedError>(result.exceptionOrNull())
    }

    private fun stubRefresh(
        stories: List<Story>,
        unreadHashes: List<String> = listOf(unreadStory.story_hash),
        starredHashes: List<String> = listOf(starredStory.story_hash),
    ) {
        coEvery { newsBlur.feeds() }.returns(Response.success(feedsResponse))
        stubArticleState(unreadHashes = unreadHashes, starredHashes = starredHashes)
        coEvery { newsBlur.riverStories(storyHashes = any()) }.returns(
            Response.success(StoriesResponse(stories = stories))
        )
    }

    private fun stubArticleState(
        unreadHashes: List<String> = emptyList(),
        starredHashes: List<String> = emptyList(),
    ) {
        coEvery { newsBlur.unreadStoryHashes() }.returns(
            Response.success(
                UnreadStoryHashesResponse(
                    unread_feed_story_hashes = unreadHashes.groupBy { it.substringBefore(":") },
                    authenticated = true,
                )
            )
        )
        coEvery { newsBlur.starredStoryHashes() }.returns(
            Response.success(
                StarredStoryHashesResponse(starred_story_hashes = starredHashes, authenticated = true)
            )
        )
        coEvery { newsBlur.riverStories(storyHashes = any()) }.returns(
            Response.success(StoriesResponse(stories = emptyList()))
        )
    }

    private fun folderNames(feedID: String): List<String> {
        return database.feedsQueries.tagged()
            .executeAsList()
            .filter { it.id == feedID }
            .mapNotNull { it.name }
    }

    private fun success() = Response.success(CodeResponse(code = 1))

    private fun <T> forbidden(): Response<T> = Response.error(403, "".toResponseBody())
}
