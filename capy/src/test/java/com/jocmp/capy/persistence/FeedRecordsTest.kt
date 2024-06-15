package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNull

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

}
