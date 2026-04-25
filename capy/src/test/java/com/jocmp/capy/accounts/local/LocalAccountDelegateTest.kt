package com.jocmp.capy.accounts.local

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.InMemoryDataStore
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.rssItemFixture
import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.ConditionalGetInfo
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssChannelResult
import com.jocmp.rssparser.model.RssItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocalAccountDelegateTest {
    private val accountID = "777"
    private val httpClient = mockk<OkHttpClient>()
    private lateinit var database: Database
    private lateinit var feedFinder: FeedFinder
    private lateinit var feedFixture: FeedFixture
    private lateinit var delegate: AccountDelegate

    private val item = RssItem(
        guid = null,
        title = "Let Tim Cook",
        author = "Ed Zitron",
        link = "https://www.wheresyoured.at/untitled/",
        pubDate = "Mon, 17 Jun 2024 16:41:38 GMT",
        description = "Last week, Apple announced “Apple Intelligence,” a suite of features coming to iOS 18 (the next version of the iPhone",
        content = "Last week, Apple announced “Apple Intelligence,” a suite of features coming to iOS 18 (the next version of the iPhone",
        image = null,
        audio = null,
        video = null,
        sourceName = null,
        sourceUrl = null,
        categories = emptyList(),
        itunesItemData = null,
        media = null,
        youtubeVideoID = null,
        commentsUrl = null,
        enclosures = emptyList(),
    )

    private val oldItem = item.copy(
        description = "Placeholder content",
        content = "Placeholder content",
        link = "https://www.wheresyoured.at/untitled2/",
        pubDate = "Wed, 15 Mar 2023 18:30:00 GMT"
    )

    private val channel = RssChannel(
        title = "Ed Zitron",
        link = "http://wheresyoured.at/feed",
        items = listOf(
            item,
            oldItem,
        ),
        description = null,
        image = null,
        itunesChannelData = null,
        lastBuildDate = null,
        updatePeriod = null
    )

    private lateinit var accountPreferences: AccountPreferences

    @Before
    fun setup() {
        mockkObject(CapyLog)

        accountPreferences = AccountPreferences(InMemoryDataStore())
        every { CapyLog.warn(any(), any()) }.returns(Unit)
        every { CapyLog.error(any(), any()) }.returns(Unit)
        every { CapyLog.info(any(), any()) }.returns(Unit)

        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        feedFinder = mockk<FeedFinder>()
        delegate = LocalAccountDelegate(
            database,
            httpClient,
            feedFinder = feedFinder,
            preferences = accountPreferences
        )
    }

    @Test
    fun refreshAll_updatesEntries() = runTest {
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(RssChannelResult(channel = channel, conditionalGet = ConditionalGetInfo.EMPTY)))

        FeedFixture(database).create(feedID = channel.link!!)

        delegate.refresh(ArticleFilter.default())

        val articlesCount = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsOne()
            .COUNT

        val feeds = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = 2, actual = articlesCount)
    }

    @Test
    fun refreshAll_updatesEntries_filteredByFilters() = runTest {
        accountPreferences.filterKeywords.set(setOf("Apple Intelligence"))
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(RssChannelResult(channel = channel, conditionalGet = ConditionalGetInfo.EMPTY)))

        FeedFixture(database).create(feedID = channel.link!!)

        delegate.refresh(ArticleFilter.default())

        val articlesCount = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsOne()
            .COUNT

        val feeds = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = 1, actual = articlesCount)
    }

    @Test
    fun refreshAll_doesNotChangeUpdatedAtTimestamp() = runTest {
        val mockNow = ZonedDateTime.parse("2024-12-25T09:00:00-00:00")
        mockkObject(TimeHelpers)
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(RssChannelResult(channel = channel, conditionalGet = ConditionalGetInfo.EMPTY)))
        every { TimeHelpers.nowUTC() }.returns(mockNow)

        FeedFixture(database).create(feedID = channel.link!!)

        delegate.refresh(ArticleFilter.default())

        var article = ArticleRecords(database).find(articleID = item.link!!)!!

        val firstUpdatedAt = article.updatedAt
        assertEquals(expected = mockNow, actual = firstUpdatedAt)

        val futureNow = ZonedDateTime.parse("2025-12-25T09:00:00-00:00")
        every { TimeHelpers.nowUTC() }.returns(futureNow)
        delegate.refresh(ArticleFilter.default())
        article = ArticleRecords(database).find(articleID = item.link!!)!!

        assertEquals(expected = firstUpdatedAt, actual = article.updatedAt)
    }


    @Test
    fun refreshAll_updatesEntriesWithCutoff() = runTest {
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(RssChannelResult(channel = channel, conditionalGet = ConditionalGetInfo.EMPTY)))

        FeedFixture(database).create(feedID = channel.link!!)

        delegate.refresh(
            ArticleFilter.default(),
            cutoffDate = ZonedDateTime.of(2024, 5, 1, 8, 0, 0, 0, ZoneOffset.UTC)
        )

        val articlesCount = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsOne()
            .COUNT

        val feeds = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = 1, actual = articlesCount)
        assertNotNull(ArticleRecords(database).find(articleID = item.link!!))
    }


    @Test
    fun refreshAll_whenNotModified_skipsArticleSaveAndPersistsRefreshedAt() = runTest {
        val feedID = channel.link!!
        FeedFixture(database).create(feedID = feedID, feedURL = feedID)

        val feedRecords = FeedRecords(database)
        feedRecords.updateConditionalGet(
            feedID = feedID,
            conditionalGet = ConditionalGetInfo(etag = "abc", lastModified = "Mon, 01 Jan 2024 00:00:00 GMT"),
            refreshedAt = 1_000L,
        )

        val mockNow = ZonedDateTime.parse("2024-12-25T09:00:00-00:00")
        mockkObject(TimeHelpers)
        every { TimeHelpers.nowUTC() }.returns(mockNow)

        coEvery {
            feedFinder.fetch(url = any(), conditionalGet = any())
        }.returns(
            Result.success(
                RssChannelResult(channel = null, conditionalGet = ConditionalGetInfo.EMPTY)
            )
        )

        delegate.refresh(ArticleFilter.default())

        val articleCounts = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsList()
        assertTrue(articleCounts.isEmpty())

        val stored = feedRecords.findConditionalGet(feedID)
        assertEquals(expected = "abc", actual = stored.etag)
        assertEquals(expected = "Mon, 01 Jan 2024 00:00:00 GMT", actual = stored.lastModified)

        val refreshedAt = database
            .feedsQueries
            .findConditionalGet(feedID)
            .executeAsOne()
            .conditional_get_refreshed_at
        assertEquals(expected = mockNow.toEpochSecond(), actual = refreshedAt)
    }

    @Test
    fun refreshAll_sendsStoredConditionalGetOnFetch() = runTest {
        val feedID = channel.link!!
        FeedFixture(database).create(feedID = feedID, feedURL = feedID)

        val stored = ConditionalGetInfo(etag = "xyz", lastModified = "Wed, 03 Jan 2024 12:00:00 GMT")
        FeedRecords(database).updateConditionalGet(
            feedID = feedID,
            conditionalGet = stored,
            refreshedAt = 1_000L,
        )

        val captured = slot<ConditionalGetInfo>()
        coEvery {
            feedFinder.fetch(url = any(), conditionalGet = capture(captured))
        }.returns(
            Result.success(
                RssChannelResult(channel = null, conditionalGet = ConditionalGetInfo.EMPTY)
            )
        )

        delegate.refresh(ArticleFilter.default())

        coVerify { feedFinder.fetch(url = feedID, conditionalGet = any()) }
        assertEquals(expected = stored, actual = captured.captured)
    }

    @Test
    fun refreshAll_onSuccess_persistsResponseConditionalGet() = runTest {
        val feedID = channel.link!!
        FeedFixture(database).create(feedID = feedID, feedURL = feedID)

        val responseConditionalGet = ConditionalGetInfo(
            etag = "new-etag",
            lastModified = "Thu, 04 Jan 2024 08:00:00 GMT",
        )

        coEvery {
            feedFinder.fetch(url = any(), conditionalGet = any())
        }.returns(
            Result.success(
                RssChannelResult(channel = channel, conditionalGet = responseConditionalGet)
            )
        )

        delegate.refresh(ArticleFilter.default())

        val stored = FeedRecords(database).findConditionalGet(feedID)
        assertEquals(expected = "new-etag", actual = stored.etag)
        assertEquals(expected = "Thu, 04 Jan 2024 08:00:00 GMT", actual = stored.lastModified)
    }

    @Test
    fun markRead() = runTest {
        assertTrue(delegate.markRead(listOf("777")).isSuccess)
    }

    @Test
    fun markUnread() = runTest {
        assertTrue(delegate.markUnread(listOf("777")).isSuccess)
    }

    @Test
    fun addStar() = runTest {
        assertTrue(delegate.addStar(listOf("777")).isSuccess)
    }

    @Test
    fun removeStar() = runTest {
        assertTrue(delegate.removeStar(listOf("777")).isSuccess)
    }

    @Test
    fun addFeed() = runTest {
        val url = "wheresyoured.at"

        coEvery { feedFinder.find(url) }.returns(
            Result.success(
                listOf(
                    TestFeed(
                        name = "Ed Zitron",
                        feedURL = URL(channel.link!!),
                        siteURL = null,
                    )
                )
            )
        )

        val result = delegate.addFeed(url = url) as AddFeedResult.Success
        val feed = result.feed

        assertEquals(
            expected = "Ed Zitron",
            actual = feed.title
        )
    }

    @Test
    fun addFeed_multipleChoice() = runTest {
        val url = "9to5google.com"

        val choices = listOf(
            TestFeed(
                name = "9to5Google",
                feedURL = URL("https://9to5google.com/feed")
            ),
            TestFeed(
                name = "Comments for 9to5Google",
                feedURL = URL("https://9to5google.com/comments/feed")
            ),
            TestFeed(
                name = "Stories Archive - 9to5Google",
                feedURL = URL("https://9to5google.com/web-stories/feed"),
            )
        )

        coEvery { feedFinder.find(url) }.returns(Result.success(choices))

        val result = delegate.addFeed(url = url)

        val actualTitles = (result as AddFeedResult.MultipleChoices).choices.map { it.title }

        assertEquals(expected = choices.map { it.name }, actual = actualTitles)
    }

    @Test
    fun addFeed_Failure() = runTest {
        val url = "example.com"

        coEvery { feedFinder.find(url) }.returns(Result.failure(Error("Sorry charlie")))

        val result = delegate.addFeed(url = url)

        assertTrue(result is AddFeedResult.Failure)
    }

    @Test
    fun updateFeed_modifyTitle() = runTest {
        val feed = feedFixture.create()

        val feedTitle = "The Verge Mobile Podcast"

        val updated = delegate.updateFeed(
            feed = feed,
            title = feedTitle,
            folderTitles = emptyList(),
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }

    @Test
    fun rssContentHTML() {
        val content = "Some content"
        val description = "My description"

        val result = rssItemFixture(content = content, description = description).contentHTML

        assertEquals(expected = content, actual = result)
    }

    @Test
    fun contentHTML_whenEmpty() {
        val description = "My description"

        val result = rssItemFixture(content = null, description = description).contentHTML

        assertEquals(expected = description, actual = result)
    }

    @Test
    fun summary() {
        val description = "My description"

        val result = rssItemFixture(description = description).summary

        assertEquals(expected = description, actual = result)
    }


    @Test
    fun summary_whenBlank() {
        val result = rssItemFixture(description = "").summary

        assertEquals(expected = null, actual = result)
    }


    @Test
    fun summary_whenNull() {
        val result = rssItemFixture(description = null).summary

        assertEquals(expected = null, actual = result)
    }

    private data class TestFeed(
        override val name: String,
        override val feedURL: URL,
        override val siteURL: URL? = null,
        override val faviconURL: URL? = null,
        override val itunesImageURL: String? = null,
        override val items: List<RssItem> = listOf()
    ) : Feed {
        override fun isValid() = true
    }
}

private suspend fun AccountDelegate.addFeed(url: String) =
    addFeed(url = url, null, null)
