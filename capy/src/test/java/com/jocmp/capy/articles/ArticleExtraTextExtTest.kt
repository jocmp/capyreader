package com.jocmp.capy.articles

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleExtraTextExtTest {
    private lateinit var database: Database
    private lateinit var articleFixture: ArticleFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build()
        articleFixture = ArticleFixture(database)
    }

    @Test
    fun `shares title and URL`() {
        val article = articleFixture.create(
            title = "TitleHere",
            url = "https://example.com/article"
        )

        val result = article.extraText

        assertEquals("TitleHere https://example.com/article", result)
    }

    @Test
    fun `returns null if URL is null`() {
        val article = articleFixture.create(
            title = "TitleHere",
            url = null
        )

        val result = article.extraText

        assertEquals(null, result)
    }

    @Test
    fun `returns URL if title is blank`() {
        val article = articleFixture.create(
            title = "",
            url = "https://example.com/article"
        )

        val result = article.extraText

        assertEquals("https://example.com/article", result)
    }
}
