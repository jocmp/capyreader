package com.jocmp.basil.persistence

import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.InMemoryDatabaseProvider
import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import com.jocmp.basil.fixtures.ArticleFixture
import com.jocmp.basil.repeated
import com.jocmp.feedbinclient.Entry
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

        val readArticle = articles.first()

        articleRecords.markRead(readArticle.id)

        val results = articleRecords
            .byStatus
            .all(ArticleStatus.UNREAD, limit = 3, offset = 0)
            .executeAsList()

        val count = articleRecords
            .byStatus
            .count(ArticleStatus.UNREAD)
            .executeAsOne()

        val expected = articles.filter { it.id != readArticle.id }.map { it.id }.toSet()
        val actual = results.map { it.id }.toSet()

        assertEquals(expected = 2, actual = count)
        assertEquals(actual = actual, expected = expected)
    }

    @Test
    fun markUnread() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.markUnread(articleID = article.id)

        val reloaded = articleRecords.fetch(articleID = article.id)!!

        assertFalse(reloaded.read)
    }

    @Test
    fun addStar() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.addStar(articleID = article.id)

        val reloaded = articleRecords.fetch(articleID = article.id)!!

        assertTrue(reloaded.starred)
    }

    @Test
    fun removeStar() {
        val article = articleFixture.create()
        val articleRecords = ArticleRecords(database)

        articleRecords.removeStar(articleID = article.id)

        val reloaded = articleRecords.fetch(articleID = article.id)!!

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
