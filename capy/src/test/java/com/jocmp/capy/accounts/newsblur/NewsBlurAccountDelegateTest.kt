/*
 * Created by Josiah Campbell.
 */
package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.InMemoryDataStore
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.newsblurclient.NewsBlur
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class NewsBlurAccountDelegateTest {
    private val accountID = "newsblur-test"
    private lateinit var database: Database
    private lateinit var newsblur: NewsBlur
    private lateinit var preferences: AccountPreferences
    private lateinit var delegate: NewsBlurAccountDelegate

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        newsblur = mockk()
        preferences = AccountPreferences(InMemoryDataStore())
        delegate = NewsBlurAccountDelegate(database, newsblur, preferences)
    }

    @Test
    fun refresh_throwsPhase2() = runTest {
        val error = assertFails { delegate.refresh(ArticleFilter.default()) }
        assertTrue(error is NotImplementedError)
        assertTrue(error.message.orEmpty().contains("Phase 2"))
    }

    @Test
    fun markRead_throwsPhase2() = runTest {
        val error = assertFails { delegate.markRead(listOf("1")) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun markUnread_throwsPhase2() = runTest {
        val error = assertFails { delegate.markUnread(listOf("1")) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun addStar_throwsPhase2() = runTest {
        val error = assertFails { delegate.addStar(listOf("1")) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun removeStar_throwsPhase2() = runTest {
        val error = assertFails { delegate.removeStar(listOf("1")) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun addFeed_throwsPhase2() = runTest {
        val error = assertFails { delegate.addFeed(url = "https://example.com/feed", title = null, folderTitles = null) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun updateFeed_throwsPhase2() = runTest {
        val error = assertFails {
            delegate.updateFeed(
                feed = mockk<Feed>(relaxed = true),
                title = "x",
                folderTitles = emptyList(),
            )
        }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun updateFolder_throwsPhase2() = runTest {
        val error = assertFails { delegate.updateFolder(oldTitle = "a", newTitle = "b") }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun removeFeed_throwsPhase2() = runTest {
        val error = assertFails { delegate.removeFeed(feed = mockk(relaxed = true)) }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun removeFolder_throwsPhase2() = runTest {
        val error = assertFails { delegate.removeFolder(folderTitle = "x") }
        assertTrue(error is NotImplementedError)
    }

    @Test
    fun createPage_throwsPhase2() = runTest {
        val error = assertFails { delegate.createPage(url = "https://example.com") }
        assertTrue(error is NotImplementedError)
    }
}
