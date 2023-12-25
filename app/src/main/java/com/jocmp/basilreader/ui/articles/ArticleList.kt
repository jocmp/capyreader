package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.basil.Article
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleList(
    pager: Pager<Int, Article>,
    onRefresh: suspend () -> Unit,
    onSelect: suspend (articleID: String) -> Unit,
) {
    val composableScope = rememberCoroutineScope()
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val refreshScope = rememberCoroutineScope()
    val (refreshing, setRefreshing) = remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        setRefreshing(true)
        onRefresh()
        setRefreshing(false)
    }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    Box(Modifier.pullRefresh(state)) {

        LazyColumn(Modifier.fillMaxWidth()) {
            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            item?.let {
                                composableScope.launch {
                                    onSelect(it.id)
                                }
                            }
                        }
                ) {
                    Text(item?.title ?: "No title", fontSize = 20.sp)
                }
            }
        }

        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
    }
}
