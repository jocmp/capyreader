package com.capyreader.app.ui.articles

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.detail.EInkScrollbar
import com.capyreader.app.ui.articles.detail.rememberEInkScrollbarState
import com.capyreader.app.ui.articles.reader.PageDirection
import com.capyreader.app.ui.collectChangesWithCurrent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ArticleListScrollbar(
    listState: LazyListState,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enabled by appPreferences.readerOptions.enableEInkScrollbar.collectChangesWithCurrent()
    val scope = rememberCoroutineScope()

    if (!enabled) {
        LazyScrollbar(state = listState) {
            content()
        }
        return
    }

    Row {
        Box(Modifier.weight(1f)) {
            content()
        }

        EInkScrollbar(
            state = rememberEInkScrollbarState(listState),
            onPage = { direction -> scope.launch { listState.scrollPage(direction) } },
            onLine = { direction -> scope.launch { listState.scrollRow(direction) } },
        )
    }
}

private suspend fun LazyListState.scrollPage(direction: PageDirection) {
    val info = layoutInfo

    if (direction == PageDirection.FORWARD) {
        val cut = info.visibleItemsInfo.lastOrNull { it.offset + it.size > info.viewportEndOffset }
            ?: info.visibleItemsInfo.lastOrNull()
            ?: return

        if (cut.index > firstVisibleItemIndex) {
            scrollToItem(cut.index)
        } else {
            scrollBy(info.viewportSize.height.toFloat())
        }
        return
    }

    scrollBy(-info.viewportSize.height.toFloat())

    if (firstVisibleItemScrollOffset > 0) {
        scrollToItem(firstVisibleItemIndex + 1)
    }
}

private suspend fun LazyListState.scrollRow(direction: PageDirection) {
    if (direction == PageDirection.FORWARD) {
        scrollToItem(firstVisibleItemIndex + 1)
        return
    }

    if (firstVisibleItemScrollOffset > 0) {
        scrollToItem(firstVisibleItemIndex)
        return
    }

    scrollToItem((firstVisibleItemIndex - 1).coerceAtLeast(0))
}
