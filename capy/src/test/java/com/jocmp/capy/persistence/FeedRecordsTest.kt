package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.awaitRepeated
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.rssparser.model.ConditionalGetInfo
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedRecordsTest {
    private lateinit var database: Database
    private lateinit var articleFixture: ArticleFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun removeFeed_cleansUpRecords() {
        val feedRecords = FeedRecords(database)
        val article = articleFixture.create()

        feedRecords.removeFeed(feedID = article.feedID)

        val result = database
            .articlesQueries
            .findBy(articleID = article.id)
            .executeAsOneOrNull()

        assertNull(result)
    }

    @Test
    fun updateStickyFullContent() = runTest {
        val records = FeedRecords(database)
        var feed = FeedFixture(database, records = records).create()

        assertFalse(feed.enableStickyFullContent)

        records.updateStickyFullContent(enabled = true, feedID = feed.id)

        feed = records.find(feed.id)!!

        assertTrue(feed.enableStickyFullContent)
    }

    @Test
    fun clearStickyFullContent() = runTest {
        val records = FeedRecords(database)
        val feeds = 3.awaitRepeated {
            val feed = FeedFixture(database, records = records).create()
            records.updateStickyFullContent(enabled = true, feedID = feed.id)

            records.find(feed.id)!!
        }

        assertEquals(
            expected = setOf(true),
            actual = feeds.map { it.enableStickyFullContent }.toSet()
        )

        records.clearStickyFullContent()

        val updated = feeds.map { records.find(it.id)!! }
        assertEquals(
            expected = setOf(false),
            actual = updated.map { it.enableStickyFullContent }.toSet()
        )
    }

    @Test
    fun findConditionalGet_returnsStoredValue() = runTest {
        val records = FeedRecords(database)
        val feed = FeedFixture(database, records = records).create()

        val lastModified = "Tue, 21 Jul 2026 03:47:50 GMT"

        records.updateConditionalGet(
            feedID = feed.id,
            conditionalGet = ConditionalGetInfo(etag = "abc123", lastModified = lastModified),
            refreshedAt = nowUTC().toEpochSecond(),
        )

        val result = records.findConditionalGet(feed.id)

        assertEquals(expected = "abc123", actual = result.etag)
        assertEquals(expected = lastModified, actual = result.lastModified)
    }

    @Test
    fun findConditionalGet_expiresAfter8Days() = runTest {
        val records = FeedRecords(database)
        val feed = FeedFixture(database, records = records).create()

        records.updateConditionalGet(
            feedID = feed.id,
            conditionalGet = ConditionalGetInfo(etag = "abc123", lastModified = "Tue, 21 Jul 2026 03:47:50 GMT"),
            refreshedAt = nowUTC().minusDays(9).toEpochSecond(),
        )

        val result = records.findConditionalGet(feed.id)

        assertTrue(result.isEmpty)
    }
}
