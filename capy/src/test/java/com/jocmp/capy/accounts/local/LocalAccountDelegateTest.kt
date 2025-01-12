package com.jocmp.capy.accounts.local

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FakeFaviconFetcher
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.rssItemFixture
import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
        commentsUrl = null,
    )

    private val oldItem = item.copy(
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

    @Before
    fun setup() {
        mockkObject(CapyLog)
        every { CapyLog.warn(any(), any()) }.returns(Unit)

        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        feedFinder = mockk<FeedFinder>()
        delegate = LocalAccountDelegate(
            database,
            httpClient,
            feedFinder = feedFinder,
            faviconFetcher = FakeFaviconFetcher
        )
    }

    @Test
    fun refreshAll_updatesEntries() = runTest {
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(channel))

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
    fun refreshAll_updatesEntriesWithCutoff() = runTest {
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(channel))

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
        override val items: List<RssItem> = listOf()
    ) : Feed {
        override fun isValid() = true
    }
}

private suspend fun AccountDelegate.addFeed(url: String) =
    addFeed(url = url, null, null)
