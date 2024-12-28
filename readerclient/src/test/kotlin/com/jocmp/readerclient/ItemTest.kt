package com.jocmp.readerclient

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ItemTest {
    @Test
    fun `isRead null list`() {
        assertFalse(isRead(null))
    }

    @Test
    fun `isRead empty list`() {
        assertFalse(isRead(emptyList()))
    }

    @Test
    fun `isRead no matching categories`() {
        assertFalse(
            isRead(
                listOf(
                    "user/-/state/com.google/reading-list",
                    "user/-/label/Uncategorized"
                )
            )
        )
    }

    @Test
    fun `isRead miniflux read category`() {
        assertTrue(isRead(listOf("user/1/state/com.google/read")))
    }

    @Test
    fun `isRead FreshRSS read category`() {
        assertTrue(
            isRead(
                listOf(
                    "user/-/state/com.google/reading-list",
                    "user/-/state/com.google/read",
                )
            )
        )
    }
}
