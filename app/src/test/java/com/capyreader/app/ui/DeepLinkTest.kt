package com.capyreader.app.ui

import android.net.Uri
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `parse returns null for a non-capy scheme`() {
        val result = DeepLink.parse(Uri.parse("https://example.com/article/123"))

        assertNull(result)
    }

    @Test
    fun `parse returns null for uri without data`() {
        assertNull(DeepLink.parse(null))
    }

    @Test
    fun `parse builds list and detail entries for an article link`() {
        val uri = DeepLink.articleUri(articleID = "https://example.com/post")

        val result = DeepLink.parse(uri)

        assertEquals(
            listOf(
                Route.ArticleList(ArticleFilter.default()),
                Route.ArticleDetail("https://example.com/post"),
            ),
            result,
        )
    }

    @Test
    fun `parse scopes an article link with feedID to that feed`() {
        val uri = DeepLink.articleUri(articleID = "article-1", feedID = "feed-1")

        val result = DeepLink.parse(uri, currentStatus = ArticleStatus.UNREAD)

        assertEquals(
            listOf(
                Route.ArticleList(
                    ArticleFilter.Feeds(
                        feedID = "feed-1",
                        folderTitle = null,
                        feedStatus = ArticleStatus.UNREAD,
                    )
                ),
                Route.ArticleDetail("article-1"),
            ),
            result,
        )
    }

    @Test
    fun `parse builds the all-articles list`() {
        val result = DeepLink.parse(DeepLink.articlesUri(ArticleStatus.ALL))

        assertEquals(
            listOf(Route.ArticleList(ArticleFilter.Articles(ArticleStatus.ALL))),
            result,
        )
    }

    @Test
    fun `parse builds the unread-articles list`() {
        val result = DeepLink.parse(DeepLink.articlesUri(ArticleStatus.UNREAD))

        assertEquals(
            listOf(Route.ArticleList(ArticleFilter.Articles(ArticleStatus.UNREAD))),
            result,
        )
    }
}
