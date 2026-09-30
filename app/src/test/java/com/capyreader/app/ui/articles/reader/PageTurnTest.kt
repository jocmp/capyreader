package com.capyreader.app.ui.articles.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class PageTurnTest {
    private val lines = (0 until 100).map { index ->
        PageBox(top = index * 30f, bottom = index * 30f + 30f)
    }

    @Test
    fun `next starts on the line cut off at the bottom`() {
        val next = PageTurn.next(top = 0f, height = 1000f, lines = lines, blocks = emptyList())

        assertEquals(990f, next)
    }

    @Test
    fun `next moves a full page on a clean line break`() {
        val next = PageTurn.next(top = 0f, height = 900f, lines = lines, blocks = emptyList())

        assertEquals(900f, next)
    }

    @Test
    fun `next starts on an image cut off at the bottom`() {
        val image = PageBox(top = 800f, bottom = 1200f)

        val next = PageTurn.next(top = 0f, height = 1000f, lines = emptyList(), blocks = listOf(image))

        assertEquals(800f, next)
    }

    @Test
    fun `next moves an image that fits to the next page`() {
        val image = PageBox(top = 300f, bottom = 1100f)

        val next = PageTurn.next(top = 0f, height = 1000f, lines = emptyList(), blocks = listOf(image))

        assertEquals(300f, next)
    }

    @Test
    fun `next moves an image ahead of the cut line`() {
        val image = PageBox(top = 950f, bottom = 1500f)

        val next = PageTurn.next(top = 0f, height = 1000f, lines = lines, blocks = listOf(image))

        assertEquals(950f, next)
    }

    @Test
    fun `next splits an image taller than a page`() {
        val image = PageBox(top = 200f, bottom = 1800f)

        val next = PageTurn.next(top = 0f, height = 1000f, lines = emptyList(), blocks = listOf(image))

        assertEquals(1000f, next)
    }

    @Test
    fun `previous hides the line cut off at the top`() {
        val previous = PageTurn.previous(top = 2000f, height = 1000f, lines = lines, blocks = emptyList())

        assertEquals(1020f, previous)
    }

    @Test
    fun `previous stops at the start`() {
        val previous = PageTurn.previous(top = 400f, height = 1000f, lines = lines, blocks = emptyList())

        assertEquals(0f, previous)
    }

    @Test
    fun `previous and next overlap by at most one line`() {
        val next = PageTurn.next(top = 0f, height = 1000f, lines = lines, blocks = emptyList())
        val previous = PageTurn.previous(top = next, height = 1000f, lines = lines, blocks = emptyList())

        assertEquals(0f, previous)
    }

    @Test
    fun `breaks split content into pages that start on whole lines`() {
        val breaks = PageTurn.breaks(
            start = 0f,
            height = 1000f,
            contentBottom = 3000f,
            lines = lines,
            blocks = emptyList(),
        )

        assertEquals(listOf(0f, 990f, 1980f, 2970f), breaks)
    }

    @Test
    fun `breaks keep short content on one page`() {
        val breaks = PageTurn.breaks(
            start = 0f,
            height = 1000f,
            contentBottom = 600f,
            lines = lines,
            blocks = emptyList(),
        )

        assertEquals(listOf(0f), breaks)
    }
}
