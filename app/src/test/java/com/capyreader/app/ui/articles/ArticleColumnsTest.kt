package com.capyreader.app.ui.articles

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleColumnsTest {
    @Test
    fun singleColumnMapsRowsToArticles() {
        val columns = ArticleColumns(count = 1)

        assertEquals(7, columns.rowOf(7))
        assertEquals(7, columns.firstArticleIn(7))
        assertEquals(7, columns.lastArticleIn(7))
        assertEquals(10, columns.rowCount(10))
    }

    @Test
    fun multipleColumnsGroupArticlesIntoRows() {
        val columns = ArticleColumns(count = 3)

        assertEquals(0, columns.rowOf(2))
        assertEquals(1, columns.rowOf(3))
        assertEquals(6, columns.firstArticleIn(2))
        assertEquals(8, columns.lastArticleIn(2))
    }

    @Test
    fun rowCountIncludesPartialRow() {
        val columns = ArticleColumns(count = 3)

        assertEquals(0, columns.rowCount(0))
        assertEquals(3, columns.rowCount(7))
        assertEquals(3, columns.rowCount(9))
    }

    @Test
    fun rowBeforeFirstHasNoArticles() {
        val columns = ArticleColumns(count = 2)

        assertEquals(-1, columns.lastArticleIn(-1))
    }

    @Test
    fun cardColumnCountScalesWithWidth() {
        assertEquals(1, cardColumnCount(360.dp))
        assertEquals(2, cardColumnCount(411.dp))
        assertEquals(3, cardColumnCount(640.dp))
        assertEquals(4, cardColumnCount(1280.dp))
    }
}
