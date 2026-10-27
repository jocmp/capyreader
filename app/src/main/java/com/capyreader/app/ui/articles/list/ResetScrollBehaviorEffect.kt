package com.capyreader.app.ui.articles.list

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.capyreader.app.ui.articles.ArticleScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetScrollBehaviorEffect(
    scrollState: ArticleScrollState,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val resetContentOffset by remember(scrollState) {
        derivedStateOf {
            scrollState.firstVisibleItemScrollOffset == 0 &&
                    scrollState.firstVisibleItemIndex == 0
        }
    }

    val resetScrollBehaviorOffset = {
        val maxCardSize = scrollState.visibleItems.maxOfOrNull { it.size } ?: 0
        val rowIndex = scrollState.firstVisibleItemIndex / scrollState.laneCount
        val nextContentOffset = -(maxCardSize * rowIndex).toFloat()
        scrollBehavior.state.contentOffset = nextContentOffset
    }

    LaunchedEffect(resetContentOffset) {
        if (resetContentOffset) {
            scrollBehavior.state.contentOffset = 0f
        }
    }

    LaunchedEffect(scrollState) {
        var previousIndex = scrollState.firstVisibleItemIndex
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { index ->
                val jumped = index > previousIndex && !scrollState.isScrollInProgress
                previousIndex = index
                if (jumped) {
                    resetScrollBehaviorOffset()
                }
            }
    }
}
