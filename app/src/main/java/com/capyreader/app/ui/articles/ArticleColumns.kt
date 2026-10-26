package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article

@Stable
class ArticleColumns(count: Int = 1, restored: Boolean = false) {
    var count by mutableIntStateOf(count)

    var restored = restored
        private set

    fun settle() {
        restored = false
    }

    fun rowOf(articleIndex: Int): Int {
        return articleIndex / count
    }

    fun firstArticleIn(row: Int): Int {
        return row * count
    }

    fun lastArticleIn(row: Int): Int {
        return (row + 1) * count - 1
    }

    fun rowCount(articleCount: Int): Int {
        return (articleCount + count - 1) / count
    }

    companion object {
        val Saver = Saver<ArticleColumns, Int>(
            save = { it.count },
            restore = { ArticleColumns(count = it, restored = true) },
        )
    }
}

@Composable
fun rememberArticleColumns(): ArticleColumns {
    return rememberSaveable(saver = ArticleColumns.Saver) { ArticleColumns() }
}

@Composable
fun ArticleColumnsEffect(
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    columns: ArticleColumns,
    targetCount: Int?,
) {
    val anchor = remember(listState) { RowAnchor() }
    val snapshot = articles.itemSnapshotList

    LaunchedEffect(listState, columns) {
        snapshotFlow { columns.firstArticleIn(listState.firstVisibleItemIndex) }
            .collect { index ->
                anchor.articleID = Snapshot.withoutReadObservation {
                    articles.itemSnapshotList.getOrNull(index)?.id
                }
            }
    }

    SideEffect {
        val count = targetCount ?: return@SideEffect

        if (articles.itemCount == 0) {
            if (!columns.restored) {
                columns.count = count
            }

            return@SideEffect
        }

        columns.settle()

        val dataChanged = anchor.snapshot !== snapshot
        anchor.snapshot = snapshot

        val previous = columns.count
        val countChanged = previous != count

        if (!countChanged && (!dataChanged || count == 1)) {
            return@SideEffect
        }

        val anchorIndex = snapshot.indexOfFirst { it != null && it.id == anchor.articleID }
        val firstArticle = anchoredArticle(
            anchorIndex = anchorIndex,
            fallback = listState.firstVisibleItemIndex * previous,
        )

        columns.count = count

        val row = columns.rowOf(firstArticle)

        if (row != listState.firstVisibleItemIndex) {
            listState.requestScrollToItem(
                index = row,
                scrollOffset = keptScrollOffset(listState = listState, countChanged = countChanged),
            )
        }
    }
}

private class RowAnchor {
    var articleID: String? = null
    var snapshot: Any? = null
}

private fun anchoredArticle(anchorIndex: Int, fallback: Int): Int {
    if (anchorIndex > -1) {
        return anchorIndex
    }

    return fallback
}

private fun keptScrollOffset(listState: LazyListState, countChanged: Boolean): Int {
    if (countChanged) {
        return 0
    }

    return listState.firstVisibleItemScrollOffset
}
