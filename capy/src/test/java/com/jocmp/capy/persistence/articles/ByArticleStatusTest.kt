package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.repeated
import org.junit.Before
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ByArticleStatusTest {
    private lateinit var database: Database
    private lateinit var articleRecords: ArticleRecords
    private lateinit var articleFixture: ArticleFixture
    private lateinit var feedFixture: FeedFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleRecords = ArticleRecords(database)
        articleFixture = ArticleFixture(database)
        feedFixture = FeedFixture(database)
    }

    @Test
    fun findIndex() {
        val feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}")
        val publishedAt = TimeHelpers.nowUTC()
        val articles = 5.repeated { index ->
            articleFixture.create(
                publishedAt = publishedAt.minusMinutes(index.toLong()).toEpochSecond(),
                feed = feed
            )
        }
        val lookupIndex = 4

        val article = articles[lookupIndex]

        val index = ByArticleStatus(database)
            .findIndex(
                articleID = article.id,
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
            )

        assertEquals(expected = lookupIndex, actual = index.toInt())
    }

    @Test
    fun findIndex_oldestFirst() {
        val feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}")
        val publishedAt = TimeHelpers.nowUTC().minusDays(1)
        val articles = 5.repeated { index ->
            articleFixture.create(
                publishedAt = publishedAt.plusMinutes(index.toLong()).toEpochSecond(),
                feed = feed
            )
        }
        val lookupIndex = 2

        val article = articles[lookupIndex]

        val index = ByArticleStatus(database)
            .findIndex(
                articleID = article.id,
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.OLDEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
            )

        assertEquals(expected = lookupIndex, actual = index.toInt())
    }

    @Test
    fun findIndex_missingArticle() {
        val feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}")

        val index = ByArticleStatus(database)
            .findIndex(
                articleID = "bogus",
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(1),
                query = null,
            )

        assertEquals(expected = -1, actual = index.toInt())
    }
}
