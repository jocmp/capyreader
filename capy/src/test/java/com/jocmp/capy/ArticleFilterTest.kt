package com.jocmp.capy

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ArticleFilterTest {
    @Test
    fun withStatus_copiesExistingFilter() {
        val filter = ArticleFilter.Feeds(
            feedID = "1",
            folderTitle = null,
            feedStatus = ArticleStatus.ALL
        )

        val nextFilter = filter.withStatus(status = ArticleStatus.UNREAD)

        assertNotEquals(filter.status, nextFilter.status)
        assertEquals(expected = ArticleStatus.UNREAD, actual = nextFilter.status)
    }
}
