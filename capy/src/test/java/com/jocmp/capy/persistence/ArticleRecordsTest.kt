package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.repeated
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArticleRecordsTest {
    private lateinit var database: Database
    private lateinit var articleFixture: ArticleFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun allByStatus_returnsAll() {
        val articles = 3.repeated {
            articleFixture.create()
        }

        val articleRecords = ArticleRecords(database)

        val results = articleRecords
            .byStatus
            .all(ArticleStatus.ALL, limit = 3, offset = 0)
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(ArticleStatus.ALL)
            .executeAsOne()

        val expected = articles.map { it.id }.toSet()
        val actual = results.map { it.id }.toSet()

        assertTrue(actual.isNotEmpty())
        assertEquals(actual = actual, expected = expected)
        assertEquals(expected = 3, actual = count)
    }

    @Test
    fun allByStatus_returnsUnread() {
        val articles = 3.repeated {
            articleFixture.create()
        }

        val articleRecords = ArticleRecords(database)

        val readArticleIDs = articles.take(2).map { it.id }.toSet()

        readArticleIDs.forEach {
            articleRecords.markUnread(it)
        }

        val results = articleRecords
            .byStatus
            .all(ArticleStatus.UNREAD, limit = 3, offset = 0)
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(ArticleStatus.UNREAD)
            .executeAsOne()

        val expected = readArticleIDs
        val actual = results.map { it.id }.toSet()

        assertEquals(expected = 2, actual = count)
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun markUnread() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.markUnread(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertFalse(reloaded.read)
    }

    @Test
    fun addStar() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.addStar(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertTrue(reloaded.starred)
    }

    @Test
    fun removeStar() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.removeStar(articleID = article.id)

        val reloaded = articleRecords.find(articleID = article.id)!!

        assertFalse(reloaded.starred)
    }

    @Test
    fun markAllUnread() {
        val articleIDs = 3.repeated { RandomUUID.generate() }
        val articleRecords = ArticleRecords(database)

        articleRecords.markAllUnread(articleIDs)

        val articles = articleIDs.map { id ->
            articleFixture.create(id = id)
        }

        assertTrue(articles.none { it.read })
    }
}
