package com.capyreader.app.ui.articles

import com.jocmp.capy.Article
import com.jocmp.capy.articles.StatusChange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.ZonedDateTime

class ArticleListStatusesTest {
    @Test
    fun `starts empty and records only touched articles`() {
        val statuses = ArticleListStatuses()

        assertEquals(emptyMap<String, ArticleListStatus>(), statuses.statuses.value)

        statuses.apply(StatusChange(listOf("a"), read = true))
        statuses.apply(StatusChange(listOf("a"), starred = true))

        assertEquals(
            mapOf("a" to ArticleListStatus(read = true, starred = true)),
            statuses.statuses.value,
        )
    }

    @Test
    fun `withStatus keeps the same article when nothing changes`() {
        val article = article(read = true)

        assertSame(article, article.withStatus(null))
        assertSame(article, article.withStatus(ArticleListStatus(read = true)))
        assertEquals(false, article.withStatus(ArticleListStatus(read = false)).read)
    }

    private fun article(read: Boolean): Article {
        val now = ZonedDateTime.now()

        return Article(
            id = "a",
            feedID = "feed",
            title = "Title",
            author = null,
            contentHTML = "",
            url = null,
            summary = "",
            imageURL = null,
            updatedAt = now,
            publishedAt = now,
            read = read,
            starred = false,
        )
    }
}
