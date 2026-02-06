package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.capyreader.app.ui.components.pulltoload.PullToLoadIndicator
import com.capyreader.app.ui.components.pulltoload.pullToLoad
import com.capyreader.app.ui.components.pulltoload.rememberPullToLoadState

@Composable
fun PullToNextFeedBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRequestNext: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberPullToLoadState(
        key = "feed_list",
        onLoadPrevious = null,
        onLoadNext = if (enabled) {
            { onRequestNext() }
        } else null,
    )

    Box(modifier.pullToLoad(state = state, enabled = enabled)) {
        content()
        PullToLoadIndicator(
            state = state,
            canLoadPrevious = false,
            canLoadNext = enabled,
        )
    }
}
