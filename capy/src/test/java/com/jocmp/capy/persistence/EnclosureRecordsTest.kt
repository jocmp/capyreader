package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class EnclosureRecordsTest {
    private lateinit var database: Database
    private lateinit var articleFixture: ArticleFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun create() {
        val enclosures = EnclosureRecords(database)
        val article = articleFixture.create()

        enclosures.create(
            articleID = article.id,
            url = "https://example.com/test.jpg",
            type = "image/jpeg",
            itunesImage = "https://example.com/itunes.jpg",
            itunesDurationSeconds = "3000",
        )

        val result = enclosures.byArticle(id = article.id).first()

        assertEquals(expected = "https://example.com/test.jpg", actual = result.url.toString())
        assertEquals(expected = "image/jpeg", actual = result.type)
        assertEquals(
            expected = "https://example.com/itunes.jpg",
            actual = result.itunesImage.toString()
        )
        assertEquals(expected = result.itunesDurationSeconds, actual = 3000L)
    }
}
