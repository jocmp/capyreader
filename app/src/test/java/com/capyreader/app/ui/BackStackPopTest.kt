package com.capyreader.app.ui

import androidx.navigation3.runtime.NavKey
import com.jocmp.capy.ArticleFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class BackStackPopTest {
    private val list = Route.ArticleList(ArticleFilter.default())

    @Test
    fun `pop removes the top entry`() {
        val backStack = mutableListOf<NavKey>(list, Route.Settings)

        backStack.pop()

        assertEquals(listOf(list), backStack)
    }

    @Test
    fun `pop keeps the root entry`() {
        val backStack = mutableListOf<NavKey>(list)

        backStack.pop()

        assertEquals(listOf(list), backStack)
    }

    @Test
    fun `pop with key is a no-op on a repeated call`() {
        val detail = Route.ArticleDetail("article-1")
        val backStack = mutableListOf<NavKey>(list, detail)

        backStack.pop(detail)
        backStack.pop(detail)

        assertEquals(listOf(list), backStack)
    }

    @Test
    fun `pop with key ignores an entry that is not on top`() {
        val detail = Route.ArticleDetail("article-1")
        val video = Route.VideoViewer("https://example.com/video.mp4")
        val backStack = mutableListOf<NavKey>(list, detail, video)

        backStack.pop(detail)

        assertEquals(listOf(list, detail, video), backStack)
    }

    @Test
    fun `popIf pops a top entry whose key was swapped in place`() {
        val backStack = mutableListOf<NavKey>(list, Route.ArticleDetail("article-1"))
        backStack[backStack.lastIndex] = Route.ArticleDetail("article-2")

        backStack.popIf { it is Route.ArticleDetail }
        backStack.popIf { it is Route.ArticleDetail }

        assertEquals(listOf(list), backStack)
    }
}
