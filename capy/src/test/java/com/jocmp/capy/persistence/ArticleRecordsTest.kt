package com.jocmp.capy.persistence

import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.fixtures.SavedSearchFixture
import com.jocmp.capy.reload
import com.jocmp.capy.repeated
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticleRecordsTest {
    private lateinit var database: Database
    private lateinit var articleRecords: ArticleRecords
    private lateinit var articleFixture: ArticleFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleRecords = ArticleRecords(database)
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun allByStatus_returnsAll() {
        val startTime = nowUTC().minusMonths(1)

        val articles = 3.repeated { i ->
            articleFixture.create(
                publishedAt = startTime.minusDays(i.toLong()).toEpochSecond()
            )
        }

        val articleRecords = ArticleRecords(database)

        val results = articleRecords
            .byStatus
            .all(
                ArticleStatus.ALL,
                limit = 3,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(ArticleStatus.ALL)
            .executeAsOne()

        val expected = articles.map { it.id }
        val actual = results.map { it.id }

        assertTrue(actual.isNotEmpty())
        assertEquals(
            actual = actual,
            expected = expected,
            message = sortedMessage(articles, results)
        )
        assertEquals(expected = 3, actual = count)
    }

    @Test
    fun allByStatus_returnsUnread() {
        val startTime = nowUTC().minusMonths(1)

        val articles = 3.repeated { i ->
            articleFixture.create(
                publishedAt = startTime.minusDays(i.toLong()).toEpochSecond()
            )
        }

        val articleRecords = ArticleRecords(database)

        val unread = articles.take(2)
        val unreadIDs = unread.map { it.id }

        unreadIDs.forEach {
            articleRecords.markUnread(it)
        }

        val results = articleRecords
            .byStatus
            .all(
                ArticleStatus.UNREAD,
                limit = 3,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(status = ArticleStatus.UNREAD)
            .executeAsOne()

        val expected = unreadIDs
        val actual = results.map { it.id }

        assertEquals(expected = 2, actual = count)
        assertEquals(actual = actual, expected = expected, message = sortedMessage(unread, results))
    }

    @Test
    fun allByStatus_returnsUnread_oldestFirst() {
        val startTime = nowUTC().minusMonths(1)

        val articles = 3.repeated { i ->
            articleFixture.create(
                publishedAt = startTime.minusDays(i.toLong()).toEpochSecond()
            )
        }
            .reversed()

        val articleRecords = ArticleRecords(database)

        val unread = articles.take(2)
        val unreadIDs = unread.map { it.id }

        unreadIDs.forEach {
            articleRecords.markUnread(it)
        }

        val results = articleRecords
            .byStatus
            .all(
                ArticleStatus.UNREAD,
                limit = 3,
                offset = 0,
                sortOrder = SortOrder.OLDEST_FIRST,
            )
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(status = ArticleStatus.UNREAD)
            .executeAsOne()

        val expected = unreadIDs
        val actual = results.map { it.id }

        assertEquals(expected = 2, actual = count)
        assertEquals(actual = actual, expected = expected, message = sortedMessage(unread, results))
    }

    @Test
    fun allByStatus_searchQuery() {
        2.repeated { i ->
            articleFixture.create(
                title = "Test Title $i",
                summary = "my summary $i",
            )
        }
        val articleResult = articleFixture.create(
            title = "The Pixel 9 is great — and a problem",
            summary = "On The Vergecast: AI photos, Chick-fil-A’s foray into streaming, headphone screens, and more.",
        )
        val articleRecords = ArticleRecords(database)
        val query = "problem"

        val results = articleRecords
            .byStatus
            .all(
                status = ArticleStatus.ALL,
                query = query,
                limit = 3,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(
                status = ArticleStatus.ALL,
                query = query,
            )
            .executeAsOne()

        val actualID = results.map { it.id }.first()

        assertEquals(expected = 1, actual = count)
        assertEquals(actual = actualID, expected = articleResult.id)
    }

    @Test
    fun allByStatus_searchQueryInSummary() {
        2.repeated { i ->
            articleFixture.create(
                title = "Test Title $i",
                summary = "my summary $i",
            )
        }
        val articleResult = articleFixture.create(
            title = "The Pixel 9 is great — and a problem",
            summary = "On The Vergecast: AI photos, Chick-fil-A’s foray into streaming, headphone screens, and more.",
        )
        val articleRecords = ArticleRecords(database)
        val query = "Chick-fil-A"

        val results = articleRecords
            .byStatus
            .all(
                status = ArticleStatus.ALL,
                query = query,
                limit = 3,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
            )
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(
                status = ArticleStatus.ALL,
                query = query,
            )
            .executeAsOne()

        val actualID = results.map { it.id }.first()

        assertEquals(expected = 1, actual = count)
        assertEquals(actual = actualID, expected = articleResult.id)
    }

    @Test
    fun allByFeed_searchQuery() {
        2.repeated { i ->
            articleFixture.create(
                title = "article $i  - i'm from a random feed!",
                summary = "Chick-fil-A $i",
            )
        }

        val vergeFeed = FeedFixture(database).create()

        2.repeated { i ->
            articleFixture.create(
                title = "Test Title $i",
                summary = "my summary $i",
                feed = vergeFeed,
            )
        }
        val articleResult = articleFixture.create(
            title = "The Pixel 9 is great — and a problem",
            summary = "On The Vergecast: AI photos, Chick-fil-A’s foray into streaming, headphone screens, and more.",
            feed = vergeFeed,
        )
        val articleRecords = ArticleRecords(database)
        val query = "Chick-fil-A"

        val since = OffsetDateTime.now().minusDays(1)
        val results = articleRecords
            .byFeed
            .all(
                status = ArticleStatus.ALL,
                query = query,
                feedIDs = listOf(vergeFeed.id),
                since = since,
                limit = 10,
                offset = 0,
                sortOrder = SortOrder.NEWEST_FIRST,
                priority = FeedPriority.FEED,
            )
            .executeAsList()

        val count = articleRecords
            .byFeed
            .count(
                status = ArticleStatus.ALL,
                query = query,
                feedIDs = listOf(vergeFeed.id),
                since = since,
                priority = FeedPriority.FEED,
            )
            .executeAsOne()

        val actualID = results.map { it.id }.first()

        assertEquals(expected = 1, actual = count)
        assertEquals(actual = actualID, expected = articleResult.id)
    }

    @Test
    fun markUnread() = runTest {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.markUnread(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertFalse(reloaded.read)
    }

    @Test
    fun addStar() = runTest {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.addStar(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertTrue(reloaded.starred)
    }

    @Test
    fun removeStar() = runTest {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.removeStar(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertFalse(reloaded.starred)
    }

    @Test
    fun markAllUnread() = runTest {
        val articleIDs = 3.repeated { RandomUUID.generate() }
        val articleRecords = ArticleRecords(database)
        val readArticle = articleFixture.create(read = false)

        articleIDs.forEach { id ->
            articleFixture.create(id = id, read = true)
        }

        articleRecords.markAllUnread(articleIDs)

        val articles = articleIDs.map { articleRecords.find(it)!! }

        assertTrue(articles.none { it.read })
        assertTrue(articleRecords.find(readArticle.id)!!.read)
    }

    @Test
    fun deleteOldArticles() = runTest {
        val oldPublishedAt = nowUTC().minusMonths(4).toEpochSecond()
        val articleRecords = ArticleRecords(database)

        val oldArticle = articleFixture.create(publishedAt = oldPublishedAt)
        val oldUnreadArticle = articleFixture.create(publishedAt = oldPublishedAt, read = false)
        val newArticle = articleFixture.create()
        val oldStarredArticle = articleFixture.create(publishedAt = oldPublishedAt).run {
            articleRecords.markAllStarred(listOf(id))
            articleRecords.find(id)!!
        }

        assertNotNull(articleRecords.reload(newArticle))
        assertNotNull(articleRecords.reload(oldUnreadArticle))
        assertNotNull(articleRecords.reload(oldStarredArticle))
        assertNotNull(articleRecords.reload(oldArticle))

        articleRecords.deleteOldArticles(before = nowUTC().minusDays(1))

        assertNotNull(articleRecords.reload(newArticle))
        assertNotNull(articleRecords.reload(oldUnreadArticle))
        assertNotNull(articleRecords.reload(oldStarredArticle))
        assertNull(articleRecords.reload(oldArticle))
    }

    @Test
    fun deleteOrphanedStatuses() = runTest {
        val articleRecords = ArticleRecords(database)
        val oldPublishedAt = nowUTC().minusDays(200).toEpochSecond()
        val recentPublishedAt = nowUTC().minusDays(30).toEpochSecond()

        val oldOrphanedArticle = articleFixture.create(publishedAt = oldPublishedAt)
        val recentOrphanedArticle = articleFixture.create(publishedAt = recentPublishedAt)
        val freshArticle = articleFixture.create()

        database.articlesQueries.deleteByID(oldOrphanedArticle.id)
        database.articlesQueries.deleteByID(recentOrphanedArticle.id)

        val missingBefore = articleRecords.findMissingArticles()
        assertEquals(expected = 2, actual = missingBefore.size)

        articleRecords.deleteOrphanedStatuses()

        val missingAfter = articleRecords.findMissingArticles()
        assertContentEquals(expected = missingAfter, listOf(recentOrphanedArticle.id))
        assertNotNull(articleRecords.reload(freshArticle))
    }

    @Test
    fun deleteAllArticles() = runTest {
        val oldPublishedAt = nowUTC().minusMonths(4).toEpochSecond()
        val articleRecords = ArticleRecords(database)

        val oldArticle = articleFixture.create(publishedAt = oldPublishedAt)
        val oldUnreadArticle = articleFixture.create(publishedAt = oldPublishedAt, read = false)
        val newReadArticle = articleFixture.create()
        val oldStarredArticle = articleFixture.create(publishedAt = oldPublishedAt).run {
            articleRecords.markAllStarred(listOf(id))
            articleRecords.find(id)!!
        }

        assertNotNull(articleRecords.reload(newReadArticle))
        assertNotNull(articleRecords.reload(oldUnreadArticle))
        assertNotNull(articleRecords.reload(oldStarredArticle))
        assertNotNull(articleRecords.reload(oldArticle))

        articleRecords.deleteAllArticles()

        assertNull(articleRecords.reload(oldArticle))
        assertNull(articleRecords.reload(newReadArticle))
        assertNotNull(articleRecords.reload(oldUnreadArticle))
        assertNotNull(articleRecords.reload(oldStarredArticle))
    }

    @Test
    fun createStatus() = runTest {
        val article = articleFixture.create(read = false)

        articleRecords.createStatus(
            articleID = article.id,
            updatedAt = nowUTC(),
            read = true,
        )

        assertFalse(articleRecords.reload(article)!!.read)
    }

    @Test
    fun updateStatus() = runTest {
        var article = articleFixture.create(read = false)
        val updated = nowUTC()

        articleRecords.updateStatus(
            articleID = article.id,
            updatedAt = updated,
            read = true,
            starred = false
        )

        article = articleRecords.reload(article)!!

        assertTrue(article.read)
        assertEquals(actual = article.updatedAt.toEpochSecond(), expected = updated.toEpochSecond())
    }

    @Test
    fun createNotifications() = runTest {
        val feed = FeedFixture(database).create(enableNotifications = true)
        val article = articleFixture.create(feed = feed, read = false)
        val since = article.publishedAt.minusMinutes(15)

        val notifications = articleRecords.createNotifications(since = since)

        assertEquals(actual = notifications.size, expected = 1)
    }

    @Test
    fun createNotifications_ignoresDeletedNotifications() = runTest {
        val feed = FeedFixture(database).create(enableNotifications = true)
        val article = articleFixture.create(feed = feed, read = false)
        val since = article.publishedAt.minusMinutes(15)

        val notifications = articleRecords.createNotifications(since = since)
        assertEquals(actual = notifications.size, expected = 1)

        articleRecords.dismissNotifications(listOf(article.id))
        val refreshedNotifications = articleRecords.createNotifications(since = since)

        assertEquals(actual = refreshedNotifications.size, expected = 0)
    }

    @Test
    fun createNotifications_ignoresCreatedNotifications() = runTest {
        val feed = FeedFixture(database).create(enableNotifications = true)
        val article = articleFixture.create(feed = feed, read = false)
        val since = article.publishedAt.minusMinutes(15)

        val notifications = articleRecords.createNotifications(since = since)
        assertEquals(actual = notifications.size, expected = 1)

        val refreshedNotifications = articleRecords.createNotifications(since = since)

        assertEquals(actual = refreshedNotifications.size, expected = 0)
    }

    @Test
    fun dismissStaleNotifications_deletesAllUndeletedNotifications() = runTest {
        val feed = FeedFixture(database).create(enableNotifications = true)
        val articles = 3.repeated { articleFixture.create(feed = feed, read = false) }
        val since = articles.first().publishedAt.minusMinutes(15)

        articleRecords.createNotifications(since = since)
        val activeCount = articleRecords.countActiveNotifications()
        assertEquals(actual = activeCount, expected = 3)

        articleRecords.dismissStaleNotifications()

        val updatedCount = articleRecords.countActiveNotifications()
        assertEquals(actual = updatedCount, expected = 0)
    }

    @Test
    fun countUnread_byArticleStatus() = runTest {
        val articles = 5.repeated { i ->
            articleFixture.create(
                publishedAt = nowUTC().minusDays(i.toLong()).toEpochSecond()
            )
        }

        val unreadArticles = articles.take(3)
        unreadArticles.forEach { article ->
            articleRecords.markUnread(article.id)
        }

        val filter = ArticleFilter.Articles(ArticleStatus.UNREAD)

        val count = articleRecords.countUnread(
            filter = filter,
            query = null
        ).firstOrNull()

        assertEquals(expected = 3, actual = count)
    }

    @Test
    fun countUnread_byFeed() = runTest {
        val feed1 = FeedFixture(database).create()
        val feed2 = FeedFixture(database).create()

        2.repeated {
            articleFixture.create(feed = feed1, read = true)
        }
        3.repeated { // Unread articles
            articleFixture.create(feed = feed1, read = false)
        }
        2.repeated {
            articleFixture.create(feed = feed2, read = false)
        }

        val filter = ArticleFilter.Feeds(
            feedID = feed1.id,
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )

        val count = articleRecords.countUnread(
            filter = filter,
            query = null
        ).firstOrNull()

        assertEquals(expected = 3, actual = count)
    }

    @Test
    fun countUnread_withQuery() = runTest {
        3.repeated { i ->
            articleFixture.create(
                title = "Regular article $i",
                read = false
            )
        }
        2.repeated { i ->
            articleFixture.create(
                title = "Special feature article $i",
                read = false
            )
        }

        val filter = ArticleFilter.Articles(ArticleStatus.UNREAD)

        val count = articleRecords.countUnread(
            filter = filter,
            query = "feature"
        ).firstOrNull()

        assertEquals(expected = 2, actual = count)
    }

    @Test
    fun countAllBySavedSearch() = runTest {
        val savedSearchFixture = SavedSearchFixture(database)
        val savedSearchRecords = SavedSearchRecords(database)

        val firstSearch = savedSearchFixture.create()
        val secondSearch = savedSearchFixture.create()

        val firstSearchArticles = 3.repeated { articleFixture.create(read = false) }
        val secondSearchArticles = 2.repeated { articleFixture.create(read = false) }

        firstSearchArticles.forEach { article ->
            savedSearchRecords.upsertArticle(articleID = article.id, savedSearchID = firstSearch.id)
        }
        secondSearchArticles.forEach { article ->
            savedSearchRecords.upsertArticle(
                articleID = article.id,
                savedSearchID = secondSearch.id
            )
        }

        val counts = articleRecords.countAllBySavedSearch(ArticleStatus.UNREAD).first()

        assertEquals(expected = 3, actual = counts[firstSearch.id])
        assertEquals(expected = 2, actual = counts[secondSearch.id])
    }
}

fun sortedMessage(expected: List<Article>, actual: List<Article>): String {
    return "Expected order ${expected.joinToString(", ") { it.title }}; got ${actual.joinToString(", ") { it.title }}"
}
