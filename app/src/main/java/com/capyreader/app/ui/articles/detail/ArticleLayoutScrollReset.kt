package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun resetScrollBehaviorListener(listState: LazyListState, scrollBehavior: TopAppBarScrollBehavior): () -> Unit {
    val resetContentOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset == 0 &&
                    listState.firstVisibleItemIndex == 0
        }
    }

    val resetScrollBehaviorOffset = {
        val maxCardSize = listState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.size } ?: 0
        val nextContentOffset = -(maxCardSize * listState.firstVisibleItemIndex).toFloat()
        scrollBehavior.state.contentOffset = nextContentOffset
    }

    LaunchedEffect(resetContentOffset) {
        if (resetContentOffset) {
            scrollBehavior.state.contentOffset = 0f
        }
    }

    return resetScrollBehaviorOffset
}
