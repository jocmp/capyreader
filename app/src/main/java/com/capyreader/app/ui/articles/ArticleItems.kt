package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article
import java.time.ZonedDateTime

@Stable
interface ArticleItems {
    val itemCount: Int

    val isLoaded: Boolean

    @Composable
    fun rowAt(index: Int): Article?

    fun peekID(index: Int): String?

    fun peekPublishedAt(index: Int): ZonedDateTime?

    fun idAt(index: Int): String?

    fun key(index: Int): Any

    fun indexOf(articleID: String): Int
}

@Composable
fun rememberPagingArticleItems(items: LazyPagingItems<Article>): ArticleItems {
    return remember(items) { PagingArticleItems(items) }
}

/** Workaround for https://issuetracker.google.com/issues/177245496 */
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

    // LazyPagingItems.peek throws when out of range.
    private fun peek(index: Int): Article? {
        return if (index in 0 until items.itemCount) items.peek(index) else null
    }
}
