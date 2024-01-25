package com.jocmp.basil.opml

import com.jocmp.basil.fixtures.ARS_TECHNICA_URL
import com.jocmp.basil.Account
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
import kotlin.test.assertTrue

class OPMLImporterTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private lateinit var account: Account

    @Before
    fun setup() {
        account = AccountFixture.create(parentFolder = folder)

        mockkConstructor(FeedFinder::class)
        mockkConstructor(LocalAccountDelegate::class)

        coEvery {
            anyConstructed<LocalAccountDelegate>().fetchAll(any())
        } returns emptyList()

        listOf<Feed>(
            GenericFeed(name = "Daring Fireball", url = "https://daringfireball.net/feeds/main"),
            GenericFeed(name = "BBC News - World", url = "https://feeds.bbci.co.uk/news/world/rss.xml"),
            GenericFeed(name = "NetNewsWire", url = "https://netnewswire.blog/feed.xml"),
            GenericFeed(name = "Block Club Chicago", url = "https://blockclubchicago.org/feed/"),
            GenericFeed(name = "Julia Evans", url = "https://jvns.ca/atom.xml")
        ).forEach { feed ->
            coEvery {
                constructedWith<FeedFinder>(EqMatcher(feed.feedURL)).find()
            } returns Result.success(listOf(feed))
        }
    }

    @Test
    fun `it imports feeds and folders`() = runBlocking {
        val uri = testURI("nested_import.xml")

        OPMLImporter(account).import(uri)

        assertTrue(File(uri).exists())
    }

    @Test
    fun `it compacts nested folders`() {

    }
}
