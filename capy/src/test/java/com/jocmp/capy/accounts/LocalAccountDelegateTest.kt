package com.jocmp.capy.accounts

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.feedbinclient.Tagging
import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalAccountDelegateTest {
    private val accountID = "777"
    private lateinit var database: Database
    private lateinit var feedFinder: FeedFinder
    private lateinit var feedFixture: FeedFixture

    private val channel = RssChannel(
        title = "Ed Zitron",
        link = "http://wheresyoured.at/feed",
        items = listOf(
            RssItem(
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
        ),
        description = null,
        image = null,
        itunesChannelData = null,
        lastBuildDate = null,
        updatePeriod = null
    )

    private val taggings = listOf(
        Tagging(
            id = 1,
            feed_id = 2,
            name = "Gadgets"
        )
    )


    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        feedFixture = FeedFixture(database)
        feedFinder = mockk<FeedFinder>()
    }

    @Test
    fun refreshAll_updatesEntries() = runTest {
        coEvery { feedFinder.fetch(url = any()) }.returns(Result.success(channel))

        val delegate = LocalAccountDelegate(database, feedFinder)

        FeedFixture(database).create(feedID = channel.link!!)

        delegate.refresh()

        val articles = database
            .articlesQueries
            .countAll(read = false, starred = false)
            .executeAsList()

        val feeds = database
            .feedsQueries
            .all()
            .executeAsList()

        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = 1, actual = articles.size)
    }

    @Test
    fun markRead() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)

        assertTrue(delegate.markRead(listOf("777")).isSuccess)
    }

    @Test
    fun markUnread() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)

        assertTrue(delegate.markUnread(listOf("777")).isSuccess)
    }

    @Test
    fun addStar() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)

        assertTrue(delegate.addStar(listOf("777")).isSuccess)
    }

    @Test
    fun removeStar() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)

        assertTrue(delegate.removeStar(listOf("777")).isSuccess)
    }

    @Test
    fun addFeed() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)
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
        val delegate = LocalAccountDelegate(database, feedFinder)
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
        val delegate = LocalAccountDelegate(database, feedFinder)
        val url = "example.com"

        coEvery { feedFinder.find(url) }.returns(Result.failure(Error("Sorry charlie")))

        val result = delegate.addFeed(url = url)

        assertTrue(result is AddFeedResult.Failure)
    }

    @Test
    fun updateFeed_modifyTitle() = runTest {
        val delegate = LocalAccountDelegate(database, feedFinder)
        val feed = feedFixture.create()

        val feedTitle = "The Verge Mobile Podcast"

        val updated = delegate.updateFeed(
            feed = feed,
            title = feedTitle,
            folderTitles = emptyList()
        ).getOrThrow()

        assertEquals(expected = feedTitle, actual = updated.title)
    }

    private data class TestFeed(
        override val name: String,
        override val feedURL: URL,
        override val siteURL: URL? = null
    ) : Feed {
        override fun isValid() = true
    }
}
