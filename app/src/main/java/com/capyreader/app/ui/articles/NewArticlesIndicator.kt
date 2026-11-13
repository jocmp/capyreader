package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.jocmp.capy.articles.SortOrder
import java.time.ZonedDateTime

data class ListHead(val id: String, val publishedAt: ZonedDateTime)

@Stable
class NewArticlesIndicator {
    var isVisible by mutableStateOf(false)
        private set

    private var newest: ListHead? = null

    fun update(head: ListHead?, firstVisibleID: String?, presentedID: String?) {
        head ?: return

        if (firstVisibleID != presentedID) {
            return
        }

        val seen = newest
        newest = head

        if (firstVisibleID == head.id) {
            isVisible = false
        } else if (seen != null && head.publishedAt.isAfter(seen.publishedAt)) {
            isVisible = true
        }
    }

    fun dismiss() {
        isVisible = false
    }
}

@Composable
fun rememberNewArticlesIndicator(
    articles: PresentedArticles,
    listState: LazyListState,
    enabled: Boolean,
): NewArticlesIndicator {
    val indicator = remember(articles.filter, articles.sortOrder) { NewArticlesIndicator() }
    val items = articles.items
    val tracking = enabled && articles.sortOrder == SortOrder.NEWEST_FIRST

    LaunchedEffect(indicator, items, tracking) {
        if (!tracking) {
            indicator.dismiss()
            return@LaunchedEffect
        }

        // IDs only, so a scroll frame doesn't build rows. snapshotFlow emits when they change.
        snapshotFlow {
            val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            val presentedID = firstVisible?.let { items.peekID(it.index) }

            Triple(items.peekID(0), firstVisible?.key as? String, presentedID)
        }.collect { (headID, firstVisibleID, presentedID) ->
            val head = headID?.let { id ->
                items.peekPublishedAt(0)?.let { ListHead(id = id, publishedAt = it) }
            }
            indicator.update(head = head, firstVisibleID = firstVisibleID, presentedID = presentedID)
        }
    }

    return indicator
}
