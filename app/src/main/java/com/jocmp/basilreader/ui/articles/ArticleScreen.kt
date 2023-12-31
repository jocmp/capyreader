package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.jocmp.basil.ArticleFilter
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import com.jocmp.basilreader.ui.components.EmptyView
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
    val scaffoldState = calculateListDetailPaneScaffoldState(
        currentPaneDestination = destination,
        scaffoldDirective = calculateArticleDirective()
    )

    val navigateToDetail = {
        setDestination(ListDetailPaneScaffoldRole.Detail)
    }

    val onComplete = {
        coroutineScope.launch {
            setDestination(ListDetailPaneScaffoldRole.List)
            drawerState.close()
        }
    }

    ArticleScaffold(
        drawerState = drawerState,
        listDetailState = scaffoldState,
        drawerPane = {
            FeedList(
                folders = viewModel.folders,
                feeds = viewModel.feeds,
                onFeedAdd = onFeedAdd,
                onFolderSelect = { viewModel.selectFolder(it) { onComplete() } },
                onFeedSelect = { viewModel.selectFeed(it) { onComplete() } }
            )
        },
        listPane = {
            viewModel.articles()?.let { pager ->
                ArticleList(
                    pager = pager,
                    onRefresh = {
                        viewModel.refreshFeed()
                    },
                    selectedStatus = viewModel.filterStatus,
                    onStatusSelect = { viewModel.selectStatus(it) },
                    onSelect = {
                        viewModel.selectArticle(it)
                        navigateToDetail()
                    }
                )
            } ?: EmptyView(fillSize = true)
        },
        detailPane = {
            ArticleView(
                article = viewModel.article,
                onBackPressed = {
                    viewModel.clearArticle()
                    setDestination(ListDetailPaneScaffoldRole.List)
                }
            )
        }
    )
}
