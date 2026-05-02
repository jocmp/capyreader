package com.jocmp.capy

import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.SyncStatusRecords
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.BeforeTest
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
    private lateinit var syncRecords: SyncStatusRecords

    @BeforeTest
    fun setup() {
        mockkObject(CapyLog)
        every { CapyLog.info(any(), any()) }.returns(Unit)
        every { CapyLog.error(any(), any()) }.returns(Unit)

        account = AccountFixture.create(
            parentFolder = folder,
            source = com.jocmp.capy.accounts.Source.FEEDBIN,
        )
        syncRecords = SyncStatusRecords(account.database)
        coEvery { account.delegate.refresh(any(), any()) }.returns(Result.success(Unit))
    }

    @Test
    fun refresh() = runTest {
        val feedFixture = FeedFixture(database = account.database)
        val feed = feedFixture.create(velocity = Velocity.TwoWeeks)
        val oldArticle = ArticleFixture(database = account.database).create(
            feed = feed,
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )

        assertEquals(account.refresh(), Result.success(Unit))

        assertNull(account.database.reload(oldArticle))
    }

    @Test
    fun refresh_velocityForeverPreservesOldArticles() = runTest {
        val feedFixture = FeedFixture(database = account.database)
        val foreverFeed = feedFixture.create()
        account.updateVelocity(feedID = foreverFeed.id, velocity = Velocity.Forever)

        val oldArticle = ArticleFixture(database = account.database).create(
            feed = foreverFeed,
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )

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
            sortOrder = SortOrder.NEWEST_FIRST,
            query = null,
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
            sortOrder = SortOrder.NEWEST_FIRST,
            query = null,
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
            sortOrder = SortOrder.NEWEST_FIRST,
            query = null,
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
            sortOrder = SortOrder.OLDEST_FIRST,
            query = null,
        )

        val result = unreadArticleIDs.takeLast(2)

        assertEquals(result, ids)
    }

    /**
     * Newest-first display order:
     *   "hij" published_at t-1
     *   "lmn" published_at t-2
     *   "efg" published_at t-3
     *   "abc" published_at t-3
     *
     * Scroll boundary is "efg". Only hij, lmn, efg should be
     * marked — not abc, even though it shares the same timestamp.
     */
    @Test
    fun `unreadArticleIDs afterID excludes timestamp sibling below boundary`() = runTest {
        val articleFixture = ArticleFixture(account.database)
        val time = nowUTC().minusMonths(1)
        val sharedTime = time.minusDays(3).toEpochSecond()

        articleFixture.create(id = "hij", read = false, publishedAt = time.minusDays(1).toEpochSecond())
        articleFixture.create(id = "lmn", read = false, publishedAt = time.minusDays(2).toEpochSecond())
        articleFixture.create(id = "efg", read = false, publishedAt = sharedTime)
        articleFixture.create(id = "abc", read = false, publishedAt = sharedTime)

        val ids = account.unreadArticleIDs(
            filter = ArticleFilter.Articles(ArticleStatus.UNREAD),
            range = MarkRead.After("efg"),
            sortOrder = SortOrder.NEWEST_FIRST,
            query = null,
        )

        assertEquals(listOf("hij", "lmn", "efg"), ids)
    }

    @Test
    fun markAllRead() = runTest {
        var articles = 5.repeated { ArticleFixture(account.database).create(read = false) }

        assertFalse(articles.all { it.read })

        val articleIDs = articles.map { it.id }
        val result = account.markAllRead(articleIDs, batchSize = 5)

        articles = articles.map { account.database.reload(it)!! }

        assertTrue(articles.all { it.read })
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { account.delegate.markRead(any()) }
    }

    @Test
    fun `markAllRead queues sync statuses`() = runTest {
        val articles = 3.repeated { ArticleFixture(account.database).create(read = false) }
        val articleIDs = articles.map { it.id }

        account.markAllRead(articleIDs)

        assertEquals(articleIDs.toSet(), syncRecords.pendingArticleIDs(SyncStatus.Key.READ).toSet())
    }

    @Test
    fun `sendArticleStatus drains pending and calls delegate`() = runTest {
        coEvery { account.delegate.markRead(any()) }.returns(Result.success(Unit))
        coEvery { account.delegate.addStar(any()) }.returns(Result.success(Unit))

        val articles = 2.repeated { ArticleFixture(account.database).create(read = false) }
        val articleIDs = articles.map { it.id }

        account.markAllRead(articleIDs)
        account.addStar(articleIDs.first())
        assertEquals(3, syncRecords.pendingCount())

        val result = account.sendArticleStatus()

        assertTrue(result.isSuccess)
        assertEquals(0, syncRecords.pendingCount())
        coVerify(exactly = 1) { account.delegate.markRead(match { it.toSet() == articleIDs.toSet() }) }
        coVerify(exactly = 1) { account.delegate.addStar(listOf(articleIDs.first())) }
    }

    @Test
    fun `sendArticleStatus on failure leaves rows for retry`() = runTest {
        coEvery { account.delegate.markRead(any()) }.returns(Result.failure(Throwable("nope")))

        val articles = 2.repeated { ArticleFixture(account.database).create(read = false) }
        val articleIDs = articles.map { it.id }
        account.markAllRead(articleIDs)

        val result = account.sendArticleStatus()

        assertTrue(result.isFailure)
        assertEquals(articleIDs.size.toLong(), syncRecords.pendingCount())
        assertEquals(articleIDs.toSet(), syncRecords.pendingArticleIDs(SyncStatus.Key.READ).toSet())
    }

    @Test
    fun `sendArticleStatus drains succeeded buckets when one fails`() = runTest {
        coEvery { account.delegate.markRead(any()) }.returns(Result.failure(Throwable("nope")))
        coEvery { account.delegate.addStar(any()) }.returns(Result.success(Unit))

        val articles = 2.repeated { ArticleFixture(account.database).create(read = false) }
        val articleIDs = articles.map { it.id }
        account.markAllRead(articleIDs)
        account.addStar(articleIDs.first())

        val result = account.sendArticleStatus()

        assertTrue(result.isFailure)
        assertEquals(articleIDs.toSet(), syncRecords.pendingArticleIDs(SyncStatus.Key.READ).toSet())
        assertTrue(syncRecords.pendingArticleIDs(SyncStatus.Key.STARRED).isEmpty())
    }

    @Test
    fun `it enables full content mode`() = runTest {
        val feed = FeedFixture(account.database).create()

        assertFalse(account.isFullContentEnabled(feed.id))

        account.enableStickyContent(feed.id)

        assertTrue(account.isFullContentEnabled(feed.id))
    }

    @Test
    fun removeFolder_onSuccess() = runTest {
        val folderTitle = "Tech"
        coEvery { account.delegate.removeFolder(folderTitle) }.returns(Result.success(Unit))

        val feed = FeedFixture(account.database).create(folderNames = listOf(folderTitle, "News"))

        assertNotNull(account.findFolder(folderTitle))

        account.removeFolder(folderTitle)

        assertNull(account.findFolder(folderTitle))
        assertNotNull(account.findFeed(feed.id))
    }

    @Test
    fun removeFolder_onFailure() = runTest {
        val folderTitle = "Tech"
        coEvery { account.delegate.removeFolder(folderTitle) }.returns(Result.failure(IOException("Sorry Charlie")))

        val feed = FeedFixture(account.database).create(folderNames = listOf(folderTitle, "News"))

        assertNotNull(account.findFolder(folderTitle))

        account.removeFolder(folderTitle)

        assertNotNull(account.findFolder(folderTitle))
        assertNotNull(account.findFeed(feed.id))
    }
}
