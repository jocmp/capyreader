package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article
import java.time.ZonedDateTime

/**
 * The articles an [ArticleList] shows: the in-memory list session ([SessionArticles]) or paged
 * search results ([PagingArticleItems]).
 */
@Stable
interface ArticleItems {
    val itemCount: Int

    /** Whether the first load finished, so an empty list means there are no articles. */
    val isLoaded: Boolean

    /** The article to render at [index], or null while it loads. */
    @Composable
    fun rowAt(index: Int): Article?

    /** The article ID at [index] without loading it, or null if it isn't loaded. */
    fun peekID(index: Int): String?

    /** When the article at [index] was published, without loading it. */
    fun peekPublishedAt(index: Int): ZonedDateTime?

    /** The article ID at [index], loading it if needed. */
    fun idAt(index: Int): String?

    /** A stable key for the row at [index]. */
    fun key(index: Int): Any

    /** The index of [articleID], or -1 when it isn't loaded. */
    fun indexOf(articleID: String): Int
}

@Composable
fun rememberPagingArticleItems(items: LazyPagingItems<Article>): ArticleItems {
    return remember(items) { PagingArticleItems(items) }
}

/**
 * After recreation, paged items first report 0 items and then the cached ones, which resets a
 * restored scroll position. Hand out a throwaway state until items arrive.
 * More info: https://issuetracker.google.com/issues/177245496.
 */
@Composable
fun ArticleItems.rememberLazyListState(): LazyListState {
    return when (itemCount) {
        0 -> remember(this) { LazyListState(0, 0) }
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}

private class PagingArticleItems(private val items: LazyPagingItems<Article>) : ArticleItems {
    override val itemCount: Int
        get() = items.itemCount

    override val isLoaded: Boolean
        get() = items.loadState.refresh is LoadState.NotLoading

    @Composable
    override fun rowAt(index: Int): Article? = items[index]

    override fun peekID(index: Int): String? = peek(index)?.id

    override fun peekPublishedAt(index: Int): ZonedDateTime? = peek(index)?.publishedAt

    override fun idAt(index: Int): String? = items[index]?.id

    override fun key(index: Int): Any = items.peek(index)?.id ?: index

    override fun indexOf(articleID: String): Int {
        return items.itemSnapshotList.indexOfFirst { it?.id == articleID }
    }

    /** [LazyPagingItems.peek] throws outside the list, unlike the other lookups here. */
    private fun peek(index: Int): Article? {
        return if (index in 0 until items.itemCount) items.peek(index) else null
    }
}
