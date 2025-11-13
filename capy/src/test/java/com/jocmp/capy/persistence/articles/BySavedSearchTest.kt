package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.SavedSearchFixture
import com.jocmp.capy.persistence.ArticleRecords
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class BySavedSearchTest {
    private lateinit var database: Database
    private lateinit var articleRecords: ArticleRecords
    private lateinit var articleFixture: ArticleFixture
    private lateinit var savedSearchFixture: SavedSearchFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleRecords = ArticleRecords(database)
        articleFixture = ArticleFixture(database)
        savedSearchFixture = SavedSearchFixture(database)
    }

    @Test
    fun all_summaryTruncation() = runTest {
        val summary = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent turpis nisi, hendrerit in lobortis ac, cursus quis odio. Etiam gravida lacinia sodales. Ut sodales orci a auctor blandit. Pellentesque ultrices faucibus magna sed rhoncus. Praesent vulputate finibus auctor. Sed a neque nec odio imperdiet finibus vitae ac ipsum. Cras mollis tincidunt suscipit. Donec quis dui eget sem ultrices faucibus eget efficitur lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae;
        """.trimIndent()

        val search = savedSearchFixture.create()
        val article = articleFixture.create(summary = summary)
            .apply {
                savedSearchFixture.createSavedSearchArticle(articleID = id, id = search.id)
            }

        val expectedSummary = summary.take(250)

        val articles = BySavedSearch(database)
            .all(
                savedSearchID = search.id,
                status = ArticleStatus.ALL,
                sortOrder = SortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
                limit = 1,
                offset = 0,
            ).executeAsList()

        assertEquals(expected = expectedSummary, actual = articles[0].summary)
    }
}
