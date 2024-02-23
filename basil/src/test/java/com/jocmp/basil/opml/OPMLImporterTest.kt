package com.jocmp.basil.opml

import com.jocmp.basil.Account
import com.jocmp.basil.TestFeedFinder
import com.jocmp.basil.accounts.FeedbinAccountDelegate
import com.jocmp.basil.fixtures.AccountFixture
import com.jocmp.basil.fixtures.GenericFeed
import com.jocmp.basil.testFile
import com.jocmp.feedfinder.parser.Feed
import io.mockk.coEvery
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class OPMLImporterTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private lateinit var account: Account

    private val sites = listOf<Feed>(
        GenericFeed(name = "Daring Fireball", url = "https://daringfireball.net/feeds/main"),
        GenericFeed(
            name = "BBC News - World",
            url = "https://feeds.bbci.co.uk/news/world/rss.xml"
        ),
        GenericFeed(name = "NetNewsWire", url = "https://netnewswire.blog/feed.xml"),
        GenericFeed(name = "Block Club Chicago", url = "https://blockclubchicago.org/feed/"),
        GenericFeed(name = "Julia Evans", url = "https://jvns.ca/atom.xml")
    ).associateBy { it.feedURL.toString() }

    private val finder = TestFeedFinder(sites)

    @Before
    fun setup() {
        mockkConstructor(FeedbinAccountDelegate::class)

        coEvery {
            anyConstructed<FeedbinAccountDelegate>().fetchAll(any())
        } returns emptyList()

        account = AccountFixture.create(parentFolder = folder, feedFinder = finder)
    }

    @Test
    fun `it imports feeds and folders`() = runBlocking {
        val uri = testFile("nested_import.xml").inputStream()

        OPMLImporter(account).import(uri) {}

        val topLevelFeeds = account.feeds.map { it.name }
        val newsFeeds = account.folders.first().feeds.map { it.name }

        assertEquals(expected = listOf("Daring Fireball", "Julia Evans"), actual = topLevelFeeds)
        assertEquals(
            expected = listOf("BBC News - World", "NetNewsWire", "Block Club Chicago"),
            actual = newsFeeds
        )
    }

    @Test
    fun `it handles feeds nested in multiple folders`() = runBlocking {
        val uri = testFile("multiple_matching_feeds.xml").inputStream()

        OPMLImporter(account).import(uri)

        val topLevelFeeds = account.feeds.map { it.name }
        val appleFeeds = account.folders.find { it.title == "Apple" }!!.feeds.map { it.name }
        val blogFeeds = account.folders.find { it.title == "Blogs" }!!.feeds.map { it.name }
        val newsFeeds = account.folders.find { it.title == "News" }!!.feeds.map { it.name }

        assertEquals(expected = listOf("Julia Evans"), actual = topLevelFeeds)
        assertEquals(expected = listOf("Daring Fireball"), actual = blogFeeds)
        assertEquals(expected = listOf("Daring Fireball"), actual = appleFeeds)
        assertEquals(
            expected = listOf("BBC News - World", "NetNewsWire", "Block Club Chicago"),
            actual = newsFeeds
        )
    }
}
