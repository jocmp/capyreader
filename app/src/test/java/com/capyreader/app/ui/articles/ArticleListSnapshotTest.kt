package com.capyreader.app.ui.articles

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleListSnapshotTest {
    private val filter = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)

    @Test
    fun `neighbors come from the list order`() {
        val snapshot = ArticleListSnapshot()
        snapshot.publish(owner = this, "a", "b", "c")

        assertEquals(null to "b", snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "a"))
        assertEquals("a" to "c", snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "b"))
        assertEquals("b" to null, snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "c"))
    }

    @Test
    fun `falls back when the snapshot doesn't cover the article`() {
        val snapshot = ArticleListSnapshot()

        assertNull(snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "a"))

        snapshot.publish(owner = this, "a", "b")

        assertNull(snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "elsewhere"))
        assertNull(snapshot.neighbors(filter, SortOrder.OLDEST_FIRST, "a"))
        assertNull(
            snapshot.neighbors(
                ArticleFilter.Articles(articleStatus = ArticleStatus.STARRED),
                SortOrder.NEWEST_FIRST,
                "a",
            )
        )
    }

    @Test
    fun `clear only drops a snapshot from the same owner`() {
        val snapshot = ArticleListSnapshot()
        val current = Any()
        snapshot.publish(owner = current, "a", "b")

        snapshot.clear(owner = Any())
        assertEquals(null to "b", snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "a"))

        snapshot.clear(owner = current)
        assertNull(snapshot.neighbors(filter, SortOrder.NEWEST_FIRST, "a"))
    }

    private fun ArticleListSnapshot.publish(owner: Any, vararg articleIDs: String) {
        set(owner, filter, SortOrder.NEWEST_FIRST, articleIDs.toList(), articleIDs::indexOf)
    }
}
