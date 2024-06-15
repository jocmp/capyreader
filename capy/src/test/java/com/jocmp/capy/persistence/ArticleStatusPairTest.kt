package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleStatus
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticleStatusPairTest {
    @Test
    fun toStatusPair_allStatus() {
        val (read, starred) = ArticleStatus.ALL.toStatusPair

        assertNull(read)
        assertNull(starred)
    }

    @Test
    fun toStatusPair_unreadStatus() {
        val (read, starred) = ArticleStatus.UNREAD.toStatusPair

        assertFalse(read!!)
        assertNull(starred)
    }

    @Test
    fun toStatusPair_starredStatus() {
        val (read, starred) = ArticleStatus.STARRED.toStatusPair

        assertNull(read)
        assertTrue(starred!!)
    }
}
