package com.jocmp.capy.opml

import com.jocmp.capy.Account
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.MockFeedFinder
import com.jocmp.capy.accounts.LocalAccountDelegate
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.GenericFeed
import com.jocmp.capy.testFile
import com.jocmp.feedfinder.parser.Feed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals

class OPMLImporterTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private val accountID = "777"
    private lateinit var account: Account
    private lateinit var database: Database

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

    private val finder = MockFeedFinder(sites)

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        val delegate = LocalAccountDelegate(database = database, feedFinder = finder)

        account = AccountFixture.create(
            id = accountID,
            database = database,
            parentFolder = folder,
            accountDelegate = delegate
        )
    }

    @Test
    fun `it imports feeds and folders`() = runBlocking {
        val inputStream = testFile("nested_import.xml").inputStream()

        OPMLImporter(account).import(inputStream = inputStream)

        val topLevelFeeds = account.feeds.first().map { it.title }.toSet()
        val newsFeeds = account.folders.first().first().feeds.map { it.title }.toSet()

        assertEquals(expected = setOf("Daring Fireball", "Julia Evans"), actual = topLevelFeeds)
        assertEquals(
            expected = setOf("BBC News - World", "NetNewsWire", "Block Club Chicago"),
            actual = newsFeeds
        )
    }

    @Test
    fun `it handles feeds nested in multiple folders`() = runBlocking {
        val inputStream = testFile("multiple_matching_feeds.xml").inputStream()

        OPMLImporter(account).import(inputStream = inputStream)

        val topLevelFeeds = account.feeds.first().map { it.title }.toSet()
        val folders = account.folders.first()
        val appleFeeds = folders.find { it.title == "Apple" }!!.feeds.map { it.title }.toSet()
        val blogFeeds = folders.find { it.title == "Blogs" }!!.feeds.map { it.title }.toSet()
        val newsFeeds = folders.find { it.title == "News" }!!.feeds.map { it.title }.toSet()

        assertEquals(expected = setOf("Julia Evans"), actual = topLevelFeeds)
        assertEquals(expected = setOf("Daring Fireball"), actual = blogFeeds)
        assertEquals(expected = setOf("Daring Fireball"), actual = appleFeeds)
        assertEquals(
            expected = setOf("BBC News - World", "NetNewsWire", "Block Club Chicago"),
            actual = newsFeeds
        )
    }
}
