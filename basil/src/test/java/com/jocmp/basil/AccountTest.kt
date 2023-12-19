package com.jocmp.basil

import com.jocmp.basil.db.Database
import com.jocmp.feedfinder.FeedFinder
import io.mockk.coEvery
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AccountTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private lateinit var database: Database
    
    @Before
    fun setup() {
        mockkConstructor(FeedFinder::class)

        coEvery {
            anyConstructed<FeedFinder>().find()
        } returns FeedFinder.Result.Success(listOf(FakeParserFeed()))

        database = InMemoryDatabaseProvider().forAccount("777")
    }

    private fun buildAccount(id: String, path: File): Account {
        return Account(
            id = id,
            path = path.toURI(),
            database = database,
        )
    }

    @Test
    fun opmlFile_endsWithSubscriptions() {
        val accountPath = folder.newFile()

        val account = buildAccount(id = "777", path = accountPath)

        assertContains(account.opmlFile.path.toString(), Regex("/subscriptions.opml$"))
    }

    @Test
    fun constructor_loadsExistingFeeds() {
        val accountPath = folder.newFile()
        val accountID = "777"

        runBlocking {
            val previousInstance = buildAccount(id = accountID, path = accountPath)
            previousInstance.addFolder(title = "Test Title")
            previousInstance.addFeed(
                FeedFormEntry(
                    url = "https://theverge.com/rss.xml",
                    name = "The Verge",
                    folderTitles = listOf(),
                )
            )
        }

        val account = buildAccount(id = accountID, path = accountPath)
        val accountTitle = account.folders.first().title

        assertEquals(expected = "Test Title", actual = accountTitle)
        assertEquals(expected = 1, actual = account.feeds.size)
    }

    @Test
    fun addFeed_singleTopLevelFeed() {
        val accountPath = folder.newFile()
        val account = buildAccount(id = "777", path = accountPath)
        val entry = FeedFormEntry(
            url = "https://theverge.com/rss/index.xml",
            name = "The Verge",
            folderTitles = listOf(),
        )

        runBlocking { account.addFeed(entry) }

        assertEquals(expected = account.feeds.size, actual = 1)
        assertEquals(expected = account.folders.size, actual = 0)

        val feed = account.feeds.first()
        assertEquals(expected = entry.name, actual = entry.name)
        assertEquals(expected = entry.url, actual = feed.feedURL)
    }

    @Test
    fun addFeed_newFolder() {
        val accountPath = folder.newFile()
        val account = buildAccount(id = "777", path = accountPath)
        val entry = FeedFormEntry(
            url = "https://theverge.com/rss/index.xml",
            name = "The Verge",
            folderTitles = listOf("Tech"),
        )

        runBlocking { account.addFeed(entry) }

        assertEquals(expected = account.feeds.size, actual = 0)
        assertEquals(expected = account.folders.size, actual = 1)

        val feed = account.folders.first().feeds.first()
        assertEquals(expected = entry.name, actual = entry.name)
        assertEquals(expected = entry.url, actual = feed.feedURL)
    }

    @Test
    fun addFeed_existingFolders() {
        val accountPath = folder.newFile()
        val account = buildAccount(id = "777", path = accountPath)
        runBlocking { account.addFolder("Tech") }

        val entry = FeedFormEntry(
            url = "https://theverge.com/rss/index.xml",
            name = "The Verge",
            folderTitles = listOf("Tech"),
        )

        runBlocking { account.addFeed(entry) }

        assertEquals(expected = account.feeds.size, actual = 0)
        assertEquals(expected = account.folders.size, actual = 1)

        val feed = account.folders.first().feeds.first()
        assertEquals(expected = entry.name, actual = feed.name)
        assertEquals(expected = entry.url, actual = feed.feedURL)
    }

    @Test
    fun addFeed_multipleFolders() {
        val accountPath = folder.newFile()
        val account = buildAccount(id = "777", path = accountPath)
        runBlocking { account.addFolder("Tech") }

        val entry = FeedFormEntry(
            url = "https://theverge.com/rss/index.xml",
            name = "The Verge",
            folderTitles = listOf("Tech", "Culture"),
        )

        runBlocking { account.addFeed(entry) }

        assertEquals(expected = account.feeds.size, actual = 0)
        assertEquals(expected = account.folders.size, actual = 2)

        val techFeed = account.folders.first().feeds.first()
        val cultureFeed = account.folders.first().feeds.first()
        assertEquals(expected = entry.name, actual = techFeed.name)
        assertEquals(expected = entry.url, actual = techFeed.feedURL)
        assertEquals(techFeed, cultureFeed)
    }
}
