package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.basil.db.Articles
import com.jocmp.basilreader.ui.EmptyView
import kotlinx.coroutines.launch


@ExperimentalMaterial3AdaptiveApi
@Composable
fun ArticleList(
    pager: Pager<Int, Articles>,
    article: Articles?,
    onSelect: suspend (articleID: Long) -> Unit,
    goBack: () -> Unit
) {
    val composableScope = rememberCoroutineScope()
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    val (destination, setDestination) = rememberSaveable { mutableStateOf(ListDetailPaneScaffoldRole.List) }
    val scaffoldState = calculateListDetailPaneScaffoldState(currentPaneDestination = destination)

    val navigateToDetail = {
        setDestination(ListDetailPaneScaffoldRole.Detail)
    }

    BackHandler(article != null) {
        setDestination(ListDetailPaneScaffoldRole.List)
        goBack()
    }

    ListDetailPaneScaffold(
        scaffoldState = scaffoldState,
        listPane = {
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
                                        navigateToDetail()
                                    }
                                }
                            }
                    ) {
                        Text(item?.title ?: "No title", fontSize = 20.sp)
                    }
                }
            }
        },
        detailPane = {
            if (article != null) {
                Text(article.title ?: "")
            } else {
                EmptyView(fullWidth = true)
            }
        }
    )
}
