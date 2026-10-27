package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.ArticleListLayout
import com.capyreader.app.ui.rememberLazyListState
import com.capyreader.app.ui.rememberLazyStaggeredGridState
import com.jocmp.capy.Article

data class ArticleVisibleItem(
    val index: Int,
    val key: Any,
    val size: Int,
)

@Stable
sealed interface ArticleScrollState {
    val firstVisibleItemIndex: Int

    val firstVisibleItemScrollOffset: Int

    val isScrollInProgress: Boolean

    val visibleItems: List<ArticleVisibleItem>

    val firstVisibleItem: ArticleVisibleItem?

    val laneCount: Int

    suspend fun scrollToItem(index: Int)

    suspend fun animateScrollToItem(index: Int)

    fun requestScrollToItem(index: Int)
}

class ArticleListScrollState(val state: LazyListState) : ArticleScrollState {
    override val firstVisibleItemIndex: Int
        get() = state.firstVisibleItemIndex

    override val firstVisibleItemScrollOffset: Int
        get() = state.firstVisibleItemScrollOffset

    override val isScrollInProgress: Boolean
        get() = state.isScrollInProgress

    override val visibleItems: List<ArticleVisibleItem>
        get() = state.layoutInfo.visibleItemsInfo.map {
            ArticleVisibleItem(index = it.index, key = it.key, size = it.size)
        }

    override val firstVisibleItem: ArticleVisibleItem?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
            ArticleVisibleItem(index = it.index, key = it.key, size = it.size)
        }

    override val laneCount: Int
        get() = 1

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }

    override suspend fun animateScrollToItem(index: Int) {
        state.animateScrollToItem(index)
    }

    override fun requestScrollToItem(index: Int) {
        state.requestScrollToItem(index)
    }
}

class ArticleGridScrollState(val state: LazyStaggeredGridState) : ArticleScrollState {
    override val firstVisibleItemIndex: Int
        get() = state.firstVisibleItemIndex

    override val firstVisibleItemScrollOffset: Int
        get() = state.firstVisibleItemScrollOffset

    override val isScrollInProgress: Boolean
        get() = state.isScrollInProgress

    override val visibleItems: List<ArticleVisibleItem>
        get() = state.layoutInfo.visibleItemsInfo.map {
            ArticleVisibleItem(index = it.index, key = it.key, size = it.size.height)
        }

    override val firstVisibleItem: ArticleVisibleItem?
        get() = state.layoutInfo.visibleItemsInfo.minByOrNull { it.index }?.let {
            ArticleVisibleItem(index = it.index, key = it.key, size = it.size.height)
        }

    override val laneCount: Int
        get() = (state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.lane } ?: 0) + 1

    override suspend fun scrollToItem(index: Int) {
        state.scrollToItem(index)
    }

    override suspend fun animateScrollToItem(index: Int) {
        state.animateScrollToItem(index)
    }

    override fun requestScrollToItem(index: Int) {
        state.requestScrollToItem(index)
    }
}

@Composable
fun LazyPagingItems<Article>.rememberArticleScrollState(
    layout: ArticleListLayout,
): ArticleScrollState {
    if (layout == ArticleListLayout.CARDS) {
        val gridState = rememberLazyStaggeredGridState()

        return remember(gridState) { ArticleGridScrollState(gridState) }
    }

    val listState = rememberLazyListState()

    return remember(listState) { ArticleListScrollState(listState) }
}
