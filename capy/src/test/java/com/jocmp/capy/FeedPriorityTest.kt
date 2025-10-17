package com.jocmp.capy

import org.junit.Test
import kotlin.test.assertEquals

class FeedPriorityTest {
    @Test
    fun `inclusivePriorities returns only main for MAIN_STREAM`() {
        val expected = listOf("main")

        assertEquals(expected = expected, actual = FeedPriority.MAIN_STREAM.inclusivePriorities)
    }

    @Test
    fun `inclusivePriorities returns main and important for IMPORTANT`() {
        val expected = listOf("main", "important")

        assertEquals(expected = expected, actual = FeedPriority.IMPORTANT.inclusivePriorities)
    }

    @Test
    fun `inclusivePriorities returns main, important, and category for CATEGORY`() {
        val expected = listOf("main", "important", "category")

        assertEquals(expected = expected, actual = FeedPriority.CATEGORY.inclusivePriorities)
    }

    @Test
    fun `inclusivePriorities returns all priorities for FEED`() {
        val expected = listOf("main", "important", "category", "feed")

        assertEquals(expected = expected, actual = FeedPriority.FEED.inclusivePriorities)
    }
}
