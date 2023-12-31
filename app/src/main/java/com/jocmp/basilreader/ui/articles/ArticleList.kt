package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basilreader.R
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleList(
    pager: Pager<Int, Article>,
    onRefresh: suspend () -> Unit,
    onSelect: suspend (articleID: String) -> Unit,
    onStatusSelect: (status: ArticleStatus) -> Unit,
    selectedStatus: ArticleStatus,
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

    Scaffold(
        bottomBar = {
            ArticleFilterNavigationBar(
                selected = selectedStatus,
                onSelect = onStatusSelect
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .pullRefresh(state)
        ) {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.id }
                ) { index ->
                    val item = lazyPagingItems[index]!!
                    Box(
                        modifier = Modifier
                            .clickable {
                                composableScope.launch {
                                    onSelect(item.id)
                                }
                            }
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(item.title, fontSize = 20.sp)
                            Text(item.arrivedAt.format(DateTimeFormatter.BASIC_ISO_DATE))
                        }
                    }
                }
            }

            PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
        }
    }
}

data class ArticleStatusNavigationItem(
    val icon: Icons,
    val label: String
)
