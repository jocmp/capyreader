package com.capyreader.app.ui.articles

import java.time.ZonedDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewArticlesIndicatorTest {
    private val now = ZonedDateTime.parse("2026-10-01T12:00:00Z")
    private val first = ListHead(id = "first", publishedAt = now)
    private val newer = ListHead(id = "newer", publishedAt = now.plusMinutes(5))
    private val older = ListHead(id = "older", publishedAt = now.minusMinutes(5))

    @Test
    fun hiddenOnFirstUpdateWhileScrolled() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")

        assertFalse(indicator.isVisible)
    }

    @Test
    fun showsWhenNewerArticleArrivesWhileScrolled() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = newer, firstVisibleID = "row-20", presentedID = "row-20")

        assertTrue(indicator.isVisible)
    }

    @Test
    fun ignoresOlderHeadWhileScrolled() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = older, firstVisibleID = "row-20", presentedID = "row-20")

        assertFalse(indicator.isVisible)
    }

    @Test
    fun ignoresPagingLoadsWithSameHead() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = first, firstVisibleID = "row-120", presentedID = "row-120")

        assertFalse(indicator.isVisible)
    }

    @Test
    fun hidesWhenHeadIsVisibleAgain() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = newer, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = newer, firstVisibleID = newer.id, presentedID = newer.id)

        assertFalse(indicator.isVisible)
    }

    @Test
    fun hidesWhenInsertedHeadLandsInView() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = first.id, presentedID = first.id)
        indicator.update(head = newer, firstVisibleID = first.id, presentedID = first.id)
        indicator.update(head = newer, firstVisibleID = newer.id, presentedID = newer.id)

        assertFalse(indicator.isVisible)
    }

    @Test
    fun showsWhenInsertedAboveVisibleHead() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = first.id, presentedID = first.id)
        indicator.update(head = newer, firstVisibleID = first.id, presentedID = first.id)

        assertTrue(indicator.isVisible)
    }

    @Test
    fun waitsForLayoutToCatchUpWithInsertedHead() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = first.id, presentedID = first.id)
        indicator.update(head = newer, firstVisibleID = first.id, presentedID = newer.id)

        assertFalse(indicator.isVisible)

        indicator.update(head = newer, firstVisibleID = newer.id, presentedID = newer.id)

        assertFalse(indicator.isVisible)
    }

    @Test
    fun ignoresEmptyList() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = null, firstVisibleID = null, presentedID = null)
        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")

        assertFalse(indicator.isVisible)
    }

    @Test
    fun dismissHides() {
        val indicator = NewArticlesIndicator()

        indicator.update(head = first, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.update(head = newer, firstVisibleID = "row-20", presentedID = "row-20")
        indicator.dismiss()

        assertFalse(indicator.isVisible)
    }
}
