package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithCurrent
import org.koin.compose.koinInject

@Composable
fun ArticleListScrollbar(
    listState: LazyListState,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val wide by appPreferences.readerOptions.enableEInkScrollbar.collectChangesWithCurrent()

    LazyScrollbar(state = listState, wide = wide) {
        content()
    }
}
