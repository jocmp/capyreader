package com.capyreader.app.ui.articles

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleStatusOverlay
import com.jocmp.capy.persistence.MAX_PAGE_IDS

/**
 * Pages over a fixed list of article IDs, keyed by index. Unlike a query-backed source it never
 * invalidates itself on database writes; its owner invalidates it when the IDs change.
 */
internal class SnapshotPagingSource(
    private val articleIDs: List<String>,
    private val loadArticles: suspend (List<String>) -> List<Article>,
    private val onMissing: suspend (Set<String>) -> Unit,
    /** Called with each appended or prepended page's IDs and when its load started. */
    private val onPageLoaded: (articleIDs: List<String>, startedAt: Long) -> Unit = { _, _ -> },
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val size = minOf(params.loadSize, MAX_PAGE_IDS)
        val total = articleIDs.size

        val start = when (params) {
            is LoadParams.Prepend -> maxOf(0, params.key - size)
            else -> (params.key ?: 0).coerceIn(0, total)
        }
        val end = when (params) {
            is LoadParams.Prepend -> params.key
            else -> minOf(start + size, total)
        }

        val slice = articleIDs.subList(start, end)
        val startedAt = ArticleStatusOverlay.now()
        val articles = loadArticles(slice)

        // An article was deleted after the snapshot. Placeholder counts would no longer add up,
        // so drop the missing IDs from the owner's snapshot and reload from it.
        if (articles.size != slice.size) {
            val loadedIDs = articles.mapTo(HashSet()) { it.id }
            onMissing(slice.filterNot { it in loadedIDs }.toSet())
            return LoadResult.Invalid()
        }

        // A refresh replaces rows already on screen, so it reports nothing: dropping their
        // overrides before the new rows are shown would flash stale state.
        if (params !is LoadParams.Refresh) {
            onPageLoaded(slice, startedAt)
        }

        return LoadResult.Page(
            data = articles,
            prevKey = start.takeIf { it > 0 },
            nextKey = end.takeIf { it < total },
            itemsBefore = start,
            itemsAfter = total - end,
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val anchor = state.anchorPosition ?: return null

        return maxOf(0, anchor - state.config.initialLoadSize / 2)
    }
}
