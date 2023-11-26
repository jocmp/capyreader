package com.jocmp.basil

import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
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
            Account(id = accountID, path = accountPath).addFeed()
        }

        val account = Account(id = accountID, path = accountPath)
        val accountTitle = account.folders.first().title

        assertEquals(expected = "Test Title", actual = accountTitle)
        assertEquals(expected = account.feeds.size, actual = 1)
    }
}
