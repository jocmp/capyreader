package com.jocmp.basil.persistence

import com.jocmp.basil.InMemoryDatabaseProvider
import com.jocmp.basil.db.Database
import com.jocmp.basil.fixtures.ArticleFixture
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
