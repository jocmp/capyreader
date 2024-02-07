package com.jocmp.basilreader.ui.articles

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jocmp.basilreader.ui.components.rememberSaveableWebViewState
import com.jocmp.basilreader.ui.components.rememberWebViewNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterialApi::class)
@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onFeedAdd: () -> Unit,
    onFeedEdit: (feedID: String) -> Unit,
    onFolderEdit: (folderTitle: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
) {
    val filter = viewModel.filter
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val (destination, setDestination) =
        rememberSaveable { mutableStateOf(ListDetailPaneScaffoldRole.List) }
    val scaffoldState =
        calculateListDetailPaneScaffoldState(
            currentPaneDestination = destination,
            scaffoldDirective = calculateArticleDirective(),
        )

    val context = LocalContext.current
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberSaveableWebViewState()

    val navigateToDetail = {
        setDestination(ListDetailPaneScaffoldRole.Detail)
    }

    val onComplete = {
        coroutineScope.launch {
            setDestination(ListDetailPaneScaffoldRole.List)
            delay(200)
            drawerState.close()
        }
    }

    val refreshScope = rememberCoroutineScope()
    val (refreshing, setRefreshing) = remember { mutableStateOf(false) }

    fun refresh() =
        refreshScope.launch {
            setRefreshing(true)
            viewModel.refreshFeed()
            setRefreshing(false)
        }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    ArticleScaffold(
        drawerState = drawerState,
        listDetailState = scaffoldState,
        drawerPane = {
            FeedList(
                folders = viewModel.folders,
                feeds = viewModel.feeds,
                onAddFeed = onFeedAdd,
                onSelectFolder = {
                    viewModel.selectFolder(it)
                    onComplete()
                },
                onSelectFeed = {
                    viewModel.selectFeed(it)
                    onComplete()
                },
                onNavigateToAccounts = onNavigateToAccounts,
                onFilterSelect = viewModel::selectArticleFilter,
                articleStatus = viewModel.filterStatus
            )
        },
        listPane = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            FilterActionMenu(
                                filter = filter,
                                onFeedEdit = onFeedEdit,
                                onFolderEdit = onFolderEdit,
                                onRemoveFeed = viewModel::removeFeed,
                                onRemoveFolder = viewModel::removeFolder
                            )
                        }
                    )
                },
                bottomBar = {
                    ArticleFilterNavigationBar(
                        selected = viewModel.filterStatus,
                        onSelect = viewModel::selectStatus,
                    )
                }
            ) { innerPadding ->
                Box(
                    Modifier
                        .padding(innerPadding)
                        .pullRefresh(state)
                ) {
                    Crossfade(
                        viewModel.articles,
                        label = ""
                    ) {
                        ArticleList(
                            articles = it,
                            onSelect = { articleID ->
                                viewModel.selectArticle(articleID) {
                                    coroutineScope.launch {
                                        val html = ArticleRenderer.render(it, context)
                                        webViewNavigator.loadHtml(html)
                                        navigateToDetail()
                                    }
                                }
                            }
                        )
                    }

                    PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
                }
            }
        },
        detailPane = {
            ArticleView(
                article = viewModel.article,
                onToggleRead = viewModel::toggleArticleRead,
                onToggleStar = viewModel::toggleArticleStar,
                webViewState = webViewState,
                webViewNavigator = webViewNavigator,
                onBackPressed = {
                    viewModel.clearArticle()
                    setDestination(ListDetailPaneScaffoldRole.List)
                }
            )
        }
    )

    LaunchedEffect(webViewNavigator) {
        val html = ArticleRenderer.render(viewModel.article, context)

        if (webViewState.viewState == null) {
            webViewNavigator.loadHtml(html)
        }
    }
}
