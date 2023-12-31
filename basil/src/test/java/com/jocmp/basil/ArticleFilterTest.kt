package com.jocmp.basil

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ArticleFilterTest {
    @Test
    fun withStatus_copiesExistingFilter() {
        val articles = ArticleFilter.default()

        val nextFilter = articles.withStatus(status = ArticleFilter.Status.STARRED)

        assertNotEquals(articles.status, nextFilter.status)
        assertEquals(expected = ArticleFilter.Status.STARRED, actual = nextFilter.status)
    }
}
