package com.jocmp.basil

import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AccountTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    @Test
    fun opmlFile_endsWithSubscriptions() {
        val accountPath = folder.newFile().toURI()

        val account = Account(id = "777", path = accountPath)

        assertContains(account.opmlFile.path.toString(), Regex("/subscriptions.opml$"))
    }

    @Test
    fun constructor_loadsExistingFeeds() {
        val accountPath = folder.newFile().toURI()
        val accountID = "777"

        runBlocking {
            Account(id = accountID, path = accountPath).addFolder(title = "Test Title")
            Account(id = accountID, path = accountPath).addFeed(
                FeedFormEntry(
                    url = "https://theverge.com/rss.xml",
                    name = "The Verge",
                    folderTitles = listOf(),
                )
            )
        }

        val account = Account(id = accountID, path = accountPath)
        val accountTitle = account.folders.first().title

        assertEquals(expected = "Test Title", actual = accountTitle)
        assertEquals(expected = account.feeds.size, actual = 1)
    }

    @Test
    fun addFeed_singleTopLevelFeed() {
        val accountPath = folder.newFile().toURI()
        val account = Account(id = "777", path = accountPath)
        val entry = FeedFormEntry(
            url = "https://www.theverge.com/rss/index.xml",
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
        val accountPath = folder.newFile().toURI()
        val account = Account(id = "777", path = accountPath)
        val entry = FeedFormEntry(
            url = "https://www.theverge.com/rss/index.xml",
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
        val accountPath = folder.newFile().toURI()
        val account = Account(id = "777", path = accountPath)
        runBlocking { account.addFolder("Tech") }

        val entry = FeedFormEntry(
            url = "https://www.theverge.com/rss/index.xml",
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
        val accountPath = folder.newFile().toURI()
        val account = Account(id = "777", path = accountPath)
        runBlocking { account.addFolder("Tech") }

        val entry = FeedFormEntry(
            url = "https://www.theverge.com/rss/index.xml",
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
