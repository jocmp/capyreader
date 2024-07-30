package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.awaitRepeated
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
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

        feed = records.findBy(feed.id)!!

        assertTrue(feed.enableStickyFullContent)
    }

    @Test
    fun clearStickyFullContent() = runTest {
        val records = FeedRecords(database)
        val feeds = 3.awaitRepeated {
            val feed = FeedFixture(database, records = records).create()
            records.updateStickyFullContent(enabled = true, feedID = feed.id)

            records.findBy(feed.id)!!
        }

        assertEquals(
            expected = setOf(true),
            actual = feeds.map { it.enableStickyFullContent }.toSet()
        )

        records.clearStickyFullContent()

        val updated = feeds.map { records.findBy(it.id)!! }
        assertEquals(
            expected = setOf(false),
            actual = updated.map { it.enableStickyFullContent }.toSet()
        )
    }
}
