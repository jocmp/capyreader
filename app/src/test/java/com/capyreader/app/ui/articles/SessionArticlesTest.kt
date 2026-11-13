package com.capyreader.app.ui.articles

import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.ArticleMembership
import com.jocmp.capy.articles.ListArticle
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.articles.StatusChange
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionArticlesTest {
    private val statusChanges = MutableSharedFlow<StatusChange>(extraBufferCapacity = 16)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    private var membership = listOf<ArticleMembership>()
    private val rowsLoaded = mutableListOf<List<String>>()
    private val deleted = mutableSetOf<String>()

    private lateinit var account: Account

    @Before
    fun setUp() {
        account = mockk(relaxed = true) {
            every { statusChanges } returns this@SessionArticlesTest.statusChanges
            every { refreshes } returns this@SessionArticlesTest.refreshes
            every { listChanges } returns emptyFlow()
            coEvery { listFeeds() } returns emptyMap()
            coEvery { sessionMembership(any(), any(), any()) } answers { membership }
            coEvery { listArticlesByIDs(any(), any()) } answers {
                val ids = firstArg<List<String>>()
                rowsLoaded.add(ids)
                ids.filterNot { it in deleted }.map(::listArticle)
            }
        }
    }

    @Test
    fun `loads the session's rows in order`() = runTest {
        membership = listOf(member("a"), member("b", read = true))

        val articles = start()

        assertTrue(articles.isLoaded)
        assertEquals(listOf("a", "b"), (0 until articles.itemCount).map(articles::idAt))
        assertEquals(1, articles.indexOf("b"))
    }

    @Test
    fun `publishes the full count before rows load`() = runTest {
        membership = listOf(member("a"), member("b"))
        val rowsReady = CompletableDeferred<Unit>()
        coEvery { account.listArticlesByIDs(any(), any()) } coAnswers {
            rowsReady.await()
            firstArg<List<String>>().map(::listArticle)
        }

        val articles = start()

        assertEquals(2, articles.itemCount)
        assertFalse(articles.isLoaded)
        assertNull(articles.entryAt(0)?.article)

        rowsReady.complete(Unit)
        runCurrent()

        assertTrue(articles.isLoaded)
        assertEquals("a", articles.entryAt(0)?.article?.id)
    }

    @Test
    fun `a local change during a sync reaches rows the sync adds`() = runTest {
        membership = listOf(member("a"))
        val articles = start()
        val membershipRead = CompletableDeferred<Unit>()
        coEvery { account.sessionMembership(any(), any(), any()) } coAnswers {
            membershipRead.await()
            listOf(member("new"), member("a"))
        }

        backgroundScope.launch(StandardTestDispatcher(testScheduler)) { articles.reload() }
        runCurrent()
        statusChanges.emit(StatusChange(listOf("new"), read = true))
        runCurrent()
        membershipRead.complete(Unit)
        runCurrent()

        assertTrue(entry(articles, "new").read)
    }

    @Test
    fun `idAt skips rows that haven't loaded`() = runTest {
        membership = listOf(member("a"))
        val rowsReady = CompletableDeferred<Unit>()
        coEvery { account.listArticlesByIDs(any(), any()) } coAnswers {
            rowsReady.await()
            firstArg<List<String>>().map(::listArticle)
        }

        val articles = start()

        assertNull(articles.idAt(0))
        assertEquals("a", articles.peekID(0))
    }

    @Test
    fun `a local status change flips only that row`() = runTest {
        membership = listOf(member("a"), member("b"))
        val articles = start()

        statusChanges.emit(StatusChange(listOf("a"), read = true))
        runCurrent()

        assertTrue(entry(articles, "a").read)
        assertFalse(entry(articles, "b").read)
    }

    @Test
    fun `reload keeps existing rows and loads only new ones`() = runTest {
        membership = listOf(member("a"))
        val articles = start()
        val before = entry(articles, "a")
        rowsLoaded.clear()

        membership = listOf(member("new"), member("a"))
        articles.reload()

        assertEquals(listOf(listOf("new")), rowsLoaded)
        assertSame(before, entry(articles, "a"))
        assertEquals(0, articles.indexOf("new"))
    }

    @Test
    fun `reload takes synced statuses`() = runTest {
        membership = listOf(member("a"))
        val articles = start()

        membership = listOf(member("a", read = true, starred = true))
        articles.reload()

        assertTrue(entry(articles, "a").read)
        assertTrue(entry(articles, "a").starred)
    }

    @Test
    fun `rows deleted between the queries are skipped`() = runTest {
        membership = listOf(member("a"), member("gone"))
        deleted.add("gone")

        val articles = start()

        assertEquals(1, articles.itemCount)
        assertEquals(-1, articles.indexOf("gone"))
    }

    @Test
    fun `publishes the order for the reader`() = runTest {
        membership = listOf(member("a"), member("b"), member("c"))
        val snapshot = ArticleListSnapshot()

        start(snapshot)

        assertEquals("a" to "c", snapshot.neighbors(FILTER, SortOrder.NEWEST_FIRST, "b"))
    }

    private fun TestScope.start(snapshot: ArticleListSnapshot = ArticleListSnapshot()): SessionArticles {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val articles = SessionArticles(
            account = account,
            listSnapshot = snapshot,
            filter = FILTER,
            sortOrder = SortOrder.NEWEST_FIRST,
            since = OffsetDateTime.now(),
            isCurrent = { true },
        )
        articles.start(backgroundScope, dispatcher)
        // advanceUntilIdle skips backgroundScope work; runCurrent doesn't.
        runCurrent()

        return articles
    }

    private fun entry(articles: SessionArticles, id: String): ArticleListEntry {
        return articles.entryAt(articles.indexOf(id))!!
    }

    private fun member(id: String, read: Boolean = false, starred: Boolean = false) =
        ArticleMembership(id = id, read = read, starred = starred)

    private fun listArticle(id: String) = ListArticle(
        id = id,
        feedID = "feed",
        title = id,
        url = null,
        summary = null,
        imageURL = null,
        publishedAt = 0,
        enclosureType = null,
        updatedAt = null,
    )

    private companion object {
        val FILTER = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
    }
}
