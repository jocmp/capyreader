package com.capyreader.app.ui.articles

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleCardTest {
    @Test
    fun cardColumnCountScalesWithWidth() {
        assertEquals(1, cardColumnCount(344.dp))
        assertEquals(2, cardColumnCount(395.dp))
        assertEquals(3, cardColumnCount(624.dp))
    }

    @Test
    fun cardColumnCountIsCapped() {
        assertEquals(4, cardColumnCount(1264.dp))
    }
}
