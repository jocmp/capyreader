package com.jocmp.capy

import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private lateinit var account: Account

    @Before
    fun setup() {
        account = AccountFixture.create(parentFolder = folder)
        coEvery { account.delegate.refresh(any(), any()) }.returns(Result.success(Unit))
    }

    @Test
    fun refresh() = runTest {
        val oldArticle = ArticleFixture(database = account.database).create(
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )

        assertEquals(account.refresh(), Result.success(Unit))

        assertNull(account.database.reload(oldArticle))
    }

    @Test
    fun refresh_autoDeleteDisabled() = runTest {
        val oldArticle = ArticleFixture(database = account.database).create(
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )
        account.preferences.autoDelete.set(AutoDelete.DISABLED)

        assertEquals(account.refresh(), Result.success(Unit))
        assertNotNull(account.database.reload(oldArticle))
    }

    @Test
    fun unreadArticleIDs() = runTest {
        val articleFixture = ArticleFixture(account.database)

        articleFixture.create() // read article

        val unreadArticleIDs = 3
            .repeated { offset ->
                articleFixture.create(
                    id = "id:${offset + 1}",
                    read = false
                )
            }
            .map { it.id }

        val ids = account.unreadArticleIDs(
            filter = ArticleFilter.Articles(ArticleStatus.ALL),
            range = MarkRead.All,
            unreadSort = UnreadSortOrder.NEWEST_FIRST,
        )

        assertEquals(unreadArticleIDs.sorted(), ids.sorted())
    }

    /**
     * Take the following list from newest to oldest
     * [3, 2, 1]
     * Request articles before "2"
     * [2, 1]
     */
    @Test
    fun unreadArticleIDs_beforeID() = runTest {
        val articleFixture = ArticleFixture(account.database)
        val time = nowUTC().minusMonths(1)

        val unreadArticleIDs = 3
            .repeated { offset ->
                articleFixture.create(
                    id = "id:${offset + 1}",
                    read = false,
                    publishedAt = time.minusDays(offset.toLong()).toEpochSecond()
                )
            }
            .map { it.id }

        val ids = account.unreadArticleIDs(
            filter = ArticleFilter.Articles(ArticleStatus.UNREAD),
            range = MarkRead.Before(unreadArticleIDs[1]),
            unreadSort = UnreadSortOrder.NEWEST_FIRST,
        )

        val result = unreadArticleIDs.takeLast(2)

        assertEquals(expected = result, actual = ids)
    }

    /**
     * Take the following list from newest to oldest
     * [3, 2, 1]
     * Request articles after "2"
     * [3, 2]
     */
    @Test
    fun unreadArticleIDs_afterID() = runTest {
        val articleFixture = ArticleFixture(account.database)
        val time = nowUTC().minusMonths(1)

        val unreadArticleIDs = 3
            .repeated { offset ->
                articleFixture.create(
                    id = "id:${offset + 1}",
                    read = false,
                    publishedAt = time.minusDays(offset.toLong()).toEpochSecond()
                )
            }
            .map { it.id }

        val ids = account.unreadArticleIDs(
            filter = ArticleFilter.Articles(ArticleStatus.UNREAD),
            range = MarkRead.After(unreadArticleIDs[1]),
            unreadSort = UnreadSortOrder.NEWEST_FIRST,
        )

        val result = unreadArticleIDs.take(2)

        assertEquals(result, ids)
    }

    /**
     * Take the following list from oldest to newest
     * [1, 2, 3]
     * Request articles after "2"
     * [2, 3]
     */
    @Test
    fun unreadArticleIDs_byIDReversed() = runTest {
        val articleFixture = ArticleFixture(account.database)
        val time = nowUTC().minusMonths(1)

        val unreadArticleIDs = 3
            .repeated { offset ->
                articleFixture.create(
                    id = "${offset + 1}_${RandomUUID.generate()}",
                    read = false,
                    publishedAt = time.plusDays(offset.toLong()).toEpochSecond()
                )
            }
            .map { it.id }

        val ids = account.unreadArticleIDs(
            filter = ArticleFilter.Articles(ArticleStatus.UNREAD),
            range = MarkRead.Before(unreadArticleIDs[1]),
            unreadSort = UnreadSortOrder.OLDEST_FIRST,
        )

        val result = unreadArticleIDs.takeLast(2)

        assertEquals(result, ids)
    }

    @Test
    fun `it enables full content mode`() {
        val feed = FeedFixture(account.database).create()

        assertFalse(account.isFullContentEnabled(feed.id))

        account.enableStickyContent(feed.id)

        assertTrue(account.isFullContentEnabled(feed.id))
    }
}
