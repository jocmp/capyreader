package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.components.rememberWebViewNavigator
import com.capyreader.app.ui.fixtures.FeedPreviewFixture
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.MarkRead.All
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ArticleLayout(
    filter: ArticleFilter,
    folders: List<Folder>,
    feeds: List<Feed>,
    allFeeds: List<Feed>,
    articles: Flow<PagingData<Article>>,
    article: Article?,
    statusCount: Long,
    refreshInterval: RefreshInterval,
    onFeedRefresh: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: suspend (feedID: String) -> Unit,
    onSelectArticleFilter: () -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onSelectArticle: (articleID: String, completion: (article: Article) -> Unit) -> Unit,
    onNavigateToSettings: () -> Unit,
    onClearArticle: () -> Unit,
    onToggleArticleRead: () -> Unit,
    onToggleArticleStar: () -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    onRemoveFeed: (feedID: String, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    drawerValue: DrawerValue = DrawerValue.Closed,
    showUnauthorizedMessage: Boolean,
    onUnauthorizedDismissRequest: () -> Unit
) {
    val skipInitialRefresh = refreshInterval == RefreshInterval.MANUALLY_ONLY

    val (isInitialized, setInitialized) = rememberSaveable {
        mutableStateOf(skipInitialRefresh)
    }
    val (isUpdatePasswordDialogOpen, setUpdatePasswordDialogOpen) = rememberSaveable {
        mutableStateOf(false)
    }
    val drawerState = rememberDrawerState(drawerValue)
    val coroutineScope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator()

    val webViewNavigator = rememberWebViewNavigator()
    val listState = rememberLazyListState()
    val pagingArticles = articles.collectAsLazyPagingItems(Dispatchers.IO)
    val state = rememberPullToRefreshState()
    val snackbarHost = remember { SnackbarHostState() }
    val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
    val editSuccessMessage = stringResource(R.string.feed_action_edit_success)
    val unsubscribeMessage = stringResource(R.string.feed_action_unsubscribe_success)
    val unsubscribeErrorMessage = stringResource(R.string.unsubscribe_error)
    val currentFeed = findCurrentFeed(filter, allFeeds)
    val scrollBehavior = pinnedBehavior(filter)

    val openUpdatePasswordDialog = {
        onUnauthorizedDismissRequest()
        setUpdatePasswordDialogOpen(true)
    }

    val navigateToDetail = {
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    val openNextList = suspend {
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        listState.scrollToItem(0)
        delay(200)
        drawerState.close()
    }

    val closeDrawer = {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    val showSnackbar = { message: String ->
        coroutineScope.launch {
            snackbarHost.showSnackbar(message)
        }
    }

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onFeedRefresh {
                state.endRefresh()
                pagingArticles.refresh()
            }
        }
    }

    ArticleScaffold(
        drawerState = drawerState,
        scaffoldNavigator = scaffoldNavigator,
        drawerPane = {
            FeedList(
                folders = folders,
                feeds = feeds,
                onSelectFolder = {
                    if (!filter.isFolderSelect(it)) {
                        webViewNavigator.clearView()
                        onSelectFolder(it.title)
                        coroutineScope.launch {
                            openNextList()
                        }
                    } else {
                        closeDrawer()
                    }
                },
                onSelectFeed = {
                    coroutineScope.launch {
                        if (!filter.isFeedSelected(it)) {
                            webViewNavigator.clearView()
                            onSelectFeed(it.id)
                            openNextList()
                        } else {
                            closeDrawer()
                        }
                    }
                },
                onFeedAdded = { feedID ->
                    coroutineScope.launch {
                        onSelectFeed(feedID)
                        openNextList()

                        showSnackbar(addFeedSuccessMessage)

                        launch {
                            state.startRefresh()
                        }
                    }
                },
                onNavigateToSettings = onNavigateToSettings,
                onFilterSelect = {
                    if (!filter.hasArticlesSelected()) {
                        onSelectArticleFilter()
                        coroutineScope.launch {
                            openNextList()
                        }
                    } else {
                        closeDrawer()
                    }
                },
                filter = filter,
                statusCount = statusCount,
                onSelectStatus = onSelectStatus,
            )
        },
        listPane = {
            Scaffold(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = {
                            FilterAppBarTitle(
                                filter = filter,
                                allFeeds = allFeeds,
                                folders = folders,
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            FeedActions(
                                feed = currentFeed,
                                onMarkAllRead = {
                                    onMarkAllRead(All)
                                },
                                onFeedEdited = {
                                    showSnackbar(editSuccessMessage)
                                },
                                onRemoveFeed = { feedID ->
                                    onRemoveFeed(
                                        feedID,
                                        {
                                            showSnackbar(unsubscribeMessage)
                                        },
                                        {
                                            showSnackbar(unsubscribeErrorMessage)
                                        }
                                    )
                                },
                                onEditFailure = { message ->
                                    showSnackbar(message)
                                }
                            )
                        }
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHost)
                },
            ) { innerPadding ->
                Box(
                    Modifier
                        .padding(innerPadding)
                        .nestedScroll(state.nestedScrollConnection)
                ) {
                    ArticleList(
                        articles = pagingArticles,
                        selectedArticleKey = article?.id,
                        listState = listState,
                        onMarkAllRead = onMarkAllRead,
                        onSelect = { articleID ->
                            onSelectArticle(articleID) {
                                navigateToDetail()
                            }
                        }
                    )

                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = state,
                    )
                }
            }
        },
        detailPane = {
            ArticleView(
                article = article,
                onToggleRead = onToggleArticleRead,
                onToggleStar = onToggleArticleStar,
                webViewNavigator = webViewNavigator,
                onBackPressed = {
                    scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
                    onClearArticle()
                }
            )
        }
    )

    if (showUnauthorizedMessage) {
        UnauthorizedAlertDialog(
            onConfirm = openUpdatePasswordDialog,
            onDismissRequest = onUnauthorizedDismissRequest,
        )
    }

    if (isUpdatePasswordDialogOpen) {
        UpdateAuthDialog(
            onSuccess = { message ->
                setUpdatePasswordDialogOpen(false)
                showSnackbar(message)
            },
            onDismissRequest = {
                setUpdatePasswordDialogOpen(false)
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            state.startRefresh()

            setInitialized(true)
        }
    }

    BackHandler(canGoBackToAll(filter, article)) {
        onSelectArticleFilter()
    }
}

fun canGoBackToAll(filter: ArticleFilter, article: Article?): Boolean {
    return article == null && !filter.hasArticlesSelected()
}

fun findCurrentFeed(filter: ArticleFilter, feeds: List<Feed>): Feed? {
    if (filter is ArticleFilter.Feeds) {
        return feeds.find { it.id == filter.feedID }
    }

    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun pinnedBehavior(filter: ArticleFilter): TopAppBarScrollBehavior {
    val topAppBarState = rememberSaveable(filter, saver = TopAppBarState.Saver) {
        TopAppBarState(0f, 0f, 0f)
    }

    return pinnedScrollBehavior(topAppBarState)
}

@Preview
@Composable
fun ArticleLayoutPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedPreviewFixture().values.take(2).toList()

    MaterialTheme {
        ArticleLayout(
            filter = ArticleFilter.default(),
            allFeeds = emptyList(),
            folders = folders,
            feeds = feeds,
            articles = emptyFlow(),
            refreshInterval = RefreshInterval.MANUALLY_ONLY,
            article = null,
            statusCount = 30,
            onFeedRefresh = {},
            onSelectFolder = {},
            onSelectFeed = {},
            onSelectArticleFilter = { },
            onSelectStatus = {},
            onSelectArticle = { _, _ -> },
            onRemoveFeed = { _, _, _ -> },
            onNavigateToSettings = { },
            onClearArticle = { },
            onToggleArticleRead = { },
            onToggleArticleStar = {},
            onMarkAllRead = {},
            drawerValue = DrawerValue.Open,
            showUnauthorizedMessage = false,
            onUnauthorizedDismissRequest = {}
        )
    }
}
