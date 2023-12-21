package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import com.jocmp.basilreader.ui.components.EmptyView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onFeedAdd: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val (destination, setDestination) = rememberSaveable { mutableStateOf(ListDetailPaneScaffoldRole.List) }
    val scaffoldState = calculateListDetailPaneScaffoldState(currentPaneDestination = destination)

    val navigateToDetail = {
        setDestination(ListDetailPaneScaffoldRole.Detail)
    }

    BackHandler(viewModel.article != null) {
        setDestination(ListDetailPaneScaffoldRole.List)
        viewModel.clearArticle()
    }

    ArticleScaffold(
        drawerState = drawerState,
        listDetailState = scaffoldState,
        drawerPane = {
            FeedList(
                folders = viewModel.folders,
                feeds = viewModel.feeds,
                onFeedAdd = onFeedAdd,
                onFeedSelect = {
                    viewModel.selectFeed(it) {
                        coroutineScope.launch {
                            delay(100)
                            drawerState.close()
                        }
                    }
                }
            )
        },
        listPane = {
            viewModel.articles()?.let { pager ->
                ArticleList(
                    pager = pager,
                    onSelect = {
                        viewModel.selectArticle(it)
                        navigateToDetail()
                    }
                )
            } ?: EmptyView(fillSize = true)
        },
        detailPane = {
            ArticleView(article = viewModel.article)
        }
    )
}
