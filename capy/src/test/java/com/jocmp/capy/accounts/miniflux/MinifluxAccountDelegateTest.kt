package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.randomID
import com.jocmp.minifluxclient.Category
import com.jocmp.minifluxclient.CreateFeedRequest
import com.jocmp.minifluxclient.CreateFeedResponse
import com.jocmp.minifluxclient.Enclosure
import com.jocmp.minifluxclient.Entry
import com.jocmp.minifluxclient.EntryResultSet
import com.jocmp.minifluxclient.EntryStatus
import com.jocmp.minifluxclient.Feed
import com.jocmp.minifluxclient.Icon
import com.jocmp.minifluxclient.IconData
import com.jocmp.minifluxclient.Miniflux
import com.jocmp.minifluxclient.UpdateEntriesRequest
import com.jocmp.minifluxclient.UpdateFeedRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinifluxAccountDelegateTest {
    private val accountID = "777"
    private lateinit var database: Database
    private lateinit var miniflux: Miniflux
    private lateinit var feedFixture: FeedFixture
    private lateinit var delegate: AccountDelegate

    private val category = Category(
        id = 1,
        title = "Tech",
        user_id = 100
    )

    private val icon = Icon(
        feed_id = 2,
        icon_id = 1
    )

    private val arsTechnicaFeed = Feed(
        id = 2,
        user_id = 100,
        title = "Ars Technica",
        site_url = "https://arstechnica.com",
        feed_url = "https://feeds.arstechnica.com/arstechnica/index",
        checked_at = "2024-02-23T17:47:45.708056Z",
        etag_header = null,
        last_modified_header = null,
        parsing_error_message = null,
        parsing_error_count = 0,
        scraper_rules = null,
        rewrite_rules = null,
        crawler = false,
        blocklist_rules = null,
        keeplist_rules = null,
        user_agent = null,
        username = null,
        password = null,
        disabled = false,
        ignore_http_cache = false,
        fetch_via_proxy = false,
        category = category,
        icon = icon
    )

    private val vergeFeed = Feed(
        id = 5,
        user_id = 100,
        title = "The Verge",
        site_url = "https://theverge.com",
        feed_url = "https://www.theverge.com/rss/index.xml",
        checked_at = "2025-02-09T14:02:28.994289Z",
        etag_header = null,
        last_modified_header = null,
        parsing_error_message = null,
        parsing_error_count = 0,
        scraper_rules = null,
        rewrite_rules = null,
        crawler = false,
        blocklist_rules = null,
        keeplist_rules = null,
        user_agent = null,
        username = null,
        password = null,
        disabled = false,
        ignore_http_cache = false,
        fetch_via_proxy = false,
        category = null,
        icon = null
    )

    private val feeds = listOf(arsTechnicaFeed, vergeFeed)
    private val categories = listOf(category)

    private val arsTechnicaArticle = Entry(
        id = 4375836222,
        user_id = 100,
        feed_id = 2,
        status = EntryStatus.UNREAD,
        hash = "abc123",
        title = "Reddit admits more moderator protests could hurt its business",
        url = "https://arstechnica.com/?p=2005526",
        comments_url = null,
        published_at = "2024-02-23T17:42:38.000000Z",
        created_at = "2024-02-23T17:47:45.708056Z",
        changed_at = "2024-02-23T17:47:45.708056Z",
        content = "<p>Reddit filed to go public on Thursday (PDF), revealing various details of the social media company's inner workings.</p>",
        author = "Scharon Harding",
        share_code = null,
        starred = false,
        reading_time = 5,
        enclosures = null,
        feed = null
    )

    private val vergeArticle = Entry(
        id = 4718104685,
        user_id = 100,
        feed_id = 5,
        status = EntryStatus.READ,
        hash = "def456",
        title = "Amazfit Active 2 review: outsized bang for your buck",
        url = "https://www.theverge.com/smartwatch-review/608342/amazfit-active-2-review",
        comments_url = null,
        published_at = "2025-02-09T14:00:00.000000Z",
        created_at = "2025-02-09T14:02:28.994289Z",
        changed_at = "2025-02-09T14:02:28.994289Z",
        content = "<p>This $130 smartwatch certainly doesn't look it.</p>",
        author = "Victoria Song",
        share_code = null,
        starred = false,
        reading_time = 8,
        enclosures = listOf(
            Enclosure(
                id = 1,
                user_id = 100,
                entry_id = 4718104685,
                url = "https://www.podtrac.com/pts/redirect.mp3/pdst.fm/e/chtbl.com/track/524GE/traffic.megaphone.fm/VMP2413819050.mp3",
                mime_type = "audio/mpeg",
                size = 45678900
            )
        ),
        feed = null
    )

    private val entries = listOf(arsTechnicaArticle, vergeArticle)

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        miniflux = mockk()
        delegate = MinifluxAccountDelegate(database, miniflux)
    }

    @Test
    fun refresh_updatesEntries() = runTest {
        coEvery { miniflux.feeds() }.returns(Response.success(feeds))
        coEvery { miniflux.icon(1) }.returns(
            Response.success(IconData(id = 1, data = "image/png;base64,abc", mime_type = "image/png"))
        )
        coEvery { miniflux.entries(starred = true, limit = 100, offset = 0) }.returns(
            Response.success(
                EntryResultSet(
                    total = 0,
                    entries = emptyList()
                )
            )
        )
        coEvery { miniflux.entries(status = EntryStatus.UNREAD.value, limit = 100, offset = 0) }.returns(
            Response.success(
                EntryResultSet(
                    total = 1,
                    entries = listOf(arsTechnicaArticle)
                )
            )
        )
        coEvery {
            miniflux.entries(
                limit = 100,
                offset = 0,
                order = "published_at",
                direction = "desc"
            )
        }.returns(
            Response.success(
                EntryResultSet(
                    total = 2,
                    entries = entries
                )
            )
        )

        delegate.refresh(ArticleFilter.default())

        val articles = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsList()

        val taggedNames = database
            .feedsQueries
            .tagged()
            .executeAsList()
            .map { it.name }

        val feedsInDb = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 2, actual = feedsInDb.size)
        assertEquals(expected = listOf(null, "Tech"), actual = taggedNames.sortedWith(nullsFirst(naturalOrder())))
        assertEquals(expected = 1, actual = articles.size)

        val enclosures = EnclosureRecords(database).byArticle(vergeArticle.id.toString())
        assertEquals(expected = 1, actual = enclosures.size)
    }

    @Test
    fun refresh_IOException() = runTest {
        val networkError = SocketTimeoutException("Network timeout")
        coEvery { miniflux.feeds() }.throws(networkError)

        val result = delegate.refresh(ArticleFilter.default())

        assertEquals(result, Result.failure(networkError))
    }

    @Test
    fun markRead() = runTest {
        val id = 777L

        coEvery { miniflux.updateEntries(any()) } returns Response.success(Unit)

        delegate.markRead(listOf(id.toString()))

        coVerify {
            miniflux.updateEntries(
                UpdateEntriesRequest(
                    entry_ids = listOf(id),
                    status = EntryStatus.READ
                )
            )
        }
    }

    @Test
    fun markUnread() = runTest {
        val id = 777L

        coEvery { miniflux.updateEntries(any()) } returns Response.success(Unit)

        delegate.markUnread(listOf(id.toString()))

        coVerify {
            miniflux.updateEntries(
                UpdateEntriesRequest(
                    entry_ids = listOf(id),
                    status = EntryStatus.UNREAD
                )
            )
        }
    }

    @Test
    fun addStar() = runTest {
        val id = 777L

        coEvery { miniflux.toggleBookmark(any()) } returns Response.success(Unit)

        delegate.addStar(listOf(id.toString()))

        coVerify { miniflux.toggleBookmark(id) }
    }

    @Test
    fun removeStar() = runTest {
        val id = 777L

        coEvery { miniflux.toggleBookmark(any()) } returns Response.success(Unit)

        delegate.removeStar(listOf(id.toString()))

        coVerify { miniflux.toggleBookmark(id) }
    }

    @Test
    fun addFeed() = runTest {
        val url = "https://wheresyoured.at/feed"
        val feedID = 2819820L

        coEvery {
            miniflux.createFeed(CreateFeedRequest(feed_url = url, category_id = null))
        } returns Response.success(CreateFeedResponse(feed_id = feedID))

        coEvery { miniflux.feed(feedID) }.returns(Response.success(arsTechnicaFeed))
        coEvery { miniflux.icon(1) }.returns(
            Response.success(IconData(id = 1, data = "image/png;base64,abc", mime_type = "image/png"))
        )

        coEvery { miniflux.entries(starred = true, limit = any(), offset = any()) }.returns(
            Response.success(EntryResultSet(total = 0, entries = emptyList()))
        )
        coEvery { miniflux.entries(status = EntryStatus.UNREAD.value, limit = any(), offset = any()) }.returns(
            Response.success(EntryResultSet(total = 0, entries = emptyList()))
        )
        coEvery {
            miniflux.entries(
                limit = any(),
                offset = any(),
                order = any(),
                direction = any()
            )
        }.returns(Response.success(EntryResultSet(total = 0, entries = emptyList())))

        val result = delegate.addFeed(
            url = url,
            folderTitles = emptyList(),
            title = ""
        ) as AddFeedResult.Success
        val feed = result.feed

        assertEquals(
            expected = "Ars Technica",
            actual = feed.title
        )
    }

    @Test
    fun addFeed_Failure() = runTest {
        val url = "https://example.com/invalid"

        coEvery {
            miniflux.createFeed(CreateFeedRequest(feed_url = url, category_id = null))
        } returns Response.error(404, mockk(relaxed = true))

        val result = delegate.addFeed(url = url, folderTitles = emptyList(), title = "")

        assertTrue(result is AddFeedResult.Failure)
    }

    @Test
    fun updateFeed_modifyTitle() = runTest {
        val feed = feedFixture.create()
        val feedTitle = "The Verge Mobile Podcast"

        coEvery {
            miniflux.updateFeed(
                feedID = feed.id.toLong(),
                request = UpdateFeedRequest(title = feedTitle, category_id = null)
            )
        }.returns(Response.success(vergeFeed))

        val updated = delegate.updateFeed(
            feed = feed,
            title = feedTitle,
            folderTitles = emptyList(),
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }
}
