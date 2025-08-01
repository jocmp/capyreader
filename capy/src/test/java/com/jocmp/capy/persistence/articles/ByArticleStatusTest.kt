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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun all_summaryTruncation() = runTest {
        val summary = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent turpis nisi, hendrerit in lobortis ac, cursus quis odio. Etiam gravida lacinia sodales. Ut sodales orci a auctor blandit. Pellentesque ultrices faucibus magna sed rhoncus. Praesent vulputate finibus auctor. Sed a neque nec odio imperdiet finibus vitae ac ipsum. Cras mollis tincidunt suscipit. Donec quis dui eget sem ultrices faucibus eget efficitur lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae;
        """.trimIndent()
        val article = articleFixture.create(summary = summary)

        val expectedSummary = summary.take(250)

        val articles = ByArticleStatus(database)
            .all(
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
                limit = 1,
                offset = 0,
            ).executeAsList()

        assertEquals(expected = expectedSummary, actual = articles[0].summary)
    }

    @Test
    fun findPages() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}")
        val publishedAt = TimeHelpers.nowUTC()
        val articles = 5.repeated { index ->
            articleFixture.create(
                id = "id:$index",
                publishedAt = publishedAt.minusMinutes(index.toLong()).toEpochSecond(),
                feed = feed
            )
        }
        val lookupIndex = 4

        val article = articles[lookupIndex]

        val pages = ByArticleStatus(database)
            .findPages(
                articleID = article.id,
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
            ).firstOrNull()!!

        assertEquals(expected = "id:3", actual = pages.previousID)
        assertEquals(expected = lookupIndex, actual = pages.current)
        assertEquals(expected = null, actual = pages.nextID)
        assertEquals(expected = 5, actual = pages.size)
    }

    @Test
    fun findPages_oldestFirst() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}")
        val publishedAt = TimeHelpers.nowUTC().minusDays(1)
        val total = 5
        val articles = total.repeated { index ->
            articleFixture.create(
                id = "id:${total - index}",
                publishedAt = publishedAt.plusMinutes(index.toLong()).toEpochSecond(),
                feed = feed
            )
        }
        val lookupIndex = 2

        val article = articles[lookupIndex]

        val pages = ByArticleStatus(database)
            .findPages(
                articleID = article.id,
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.OLDEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
            ).firstOrNull()!!

        assertEquals(expected = "id:2", actual = pages.previousID)
        assertEquals(expected = lookupIndex, actual = pages.current)
        assertEquals(expected = "id:4", actual = pages.nextID)
        assertEquals(expected = total, actual = pages.size)
    }

    @Test
    fun findPages_missingArticle() = runTest {
        val pages = ByArticleStatus(database)
            .findPages(
                articleID = "bogus",
                status = ArticleStatus.ALL,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(1),
                query = null,
            ).firstOrNull()

        assertNull(pages)
    }
}
