package com.jocmp.basil.opml

import com.jocmp.basil.fixtures.ARS_TECHNICA_URL
import com.jocmp.basil.Account
import com.jocmp.basil.TestFeedFinder
import com.jocmp.basil.fixtures.ArsTechnicaFeed
import com.jocmp.basil.fixtures.THE_VERGE_URL
import com.jocmp.basil.fixtures.TheVergeFeed
import com.jocmp.basil.accounts.LocalAccountDelegate
import com.jocmp.basil.fixtures.AccountFixture
import com.jocmp.basil.fixtures.GenericFeed
import com.jocmp.basil.testURI
import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import io.mockk.EqMatcher
import io.mockk.coEvery
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        mockkConstructor(LocalAccountDelegate::class)

        coEvery {
            anyConstructed<LocalAccountDelegate>().fetchAll(any())
        } returns emptyList()

        account = AccountFixture.create(parentFolder = folder, feedFinder = finder)
    }

    @Test
    fun `it imports feeds and folders`() = runBlocking {
        val uri = testURI("nested_import.xml")

        OPMLImporter(account).import(uri)

        val topLevelFeeds = account.feeds.map { it.name }
        val newsFeeds = account.folders.first().feeds.map { it.name }

        assertEquals(expected = listOf("Daring Fireball", "Julia Evans"), actual = topLevelFeeds)
        assertEquals(
            expected = listOf("BBC News - World", "NetNewsWire", "Block Club Chicago"),
            actual = newsFeeds
        )
    }
}
