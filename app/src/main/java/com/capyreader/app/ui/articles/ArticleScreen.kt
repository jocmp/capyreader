package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.common.Media
import com.capyreader.app.common.Saver
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.feeds.FeedList
import com.capyreader.app.ui.articles.feeds.FolderActions
import com.capyreader.app.ui.articles.feeds.LocalFolderActions
import com.capyreader.app.ui.articles.list.ArticleListTopBar
import com.capyreader.app.ui.articles.list.EmptyOnboardingView
import com.capyreader.app.ui.articles.list.MarkAllReadButton
import com.capyreader.app.ui.articles.list.PullToNextFeedBox
import com.capyreader.app.ui.articles.list.resetScrollBehaviorListener
import com.capyreader.app.ui.articles.media.ArticleMediaView
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchState
import com.capyreader.app.ui.rememberLocalConnectivity
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchUI
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    viewModel: ArticleScreenViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject(),
    onNavigateToSettings: () -> Unit,
) {
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFeeds by viewModel.allFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val filter by viewModel.filter.collectAsStateWithLifecycle(appPreferences.filter.get())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val searchState by viewModel.searchState.collectAsStateWithLifecycle(SearchState.INACTIVE)
    val nextFilter by viewModel.nextFilter.collectAsStateWithLifecycle(initialValue = null)
    val afterReadAll by viewModel.afterReadAll.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val refreshInterval by appPreferences
        .refreshInterval
        .collectChangesWithDefault(appPreferences.refreshInterval.get())

    val canSwipeToNextFeed = nextFilter != null

    val articleActions = rememberArticleActions(viewModel)
    val folderActions = rememberFolderActions(viewModel)
    val connectivity = rememberLocalConnectivity()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val showOnboarding by viewModel.showOnboarding.collectAsState(false)

    val articles = viewModel.articles.collectAsLazyPagingItems()

    val onMarkAllRead = { range: MarkRead ->
        viewModel.markAllRead(
            onArticlesCleared = {
                scope.launchUI {
                    drawerState.open()
                }
            },
            searches = savedSearches,
            folders = folders,
            feeds = feeds,
            range = range,
        )
    }

    val scaffoldNavigator = rememberArticleScaffoldNavigator<String>()
    val articleID = scaffoldNavigator.currentDestination?.contentKey

    val navigateToList = {
        scope.launchUI {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    val onInitialized = { completion: () -> Unit ->
        viewModel.initialize(onComplete = completion)
    }

    val search = ArticleSearch(
        query = searchQuery,
        start = { viewModel.startSearch() },
        clear = {
            navigateToList()
            viewModel.clearSearch()
        },
        update = {
            if (articleID != null) {
                navigateToList()
            }
            viewModel.updateSearch(it)
        },
        state = searchState,
    )

    CompositionLocalProvider(
        LocalArticleActions provides articleActions,
        LocalFolderActions provides folderActions,
        LocalConnectivity provides connectivity,
        LocalArticleLookup provides ArticleLookup(
            findArticlePages = viewModel::findArticlePages
        ),
    ) {
        val openNextFeedOnReadAll = afterReadAll == AfterReadAllBehavior.OPEN_NEXT_FEED

        val skipInitialRefresh = refreshInterval == RefreshInterval.MANUALLY_ONLY

        val (isRefreshInitialized, setRefreshInitialized) = rememberSaveable {
            mutableStateOf(skipInitialRefresh)
        }
        val (isUpdatePasswordDialogOpen, setUpdatePasswordDialogOpen) = rememberSaveable {
            mutableStateOf(false)
        }
        val showMultipleColumns = scaffoldNavigator.scaffoldDirective.maxHorizontalPartitions > 1
        var isRefreshing by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
        val currentFeed = rememberCurrentFeed(filter, allFeeds)
        val scrollBehavior = rememberArticleTopBar(filter)
        var media by rememberSaveable(saver = Media.Saver) { mutableStateOf(null) }
        val focusManager = LocalFocusManager.current
        val openUpdatePasswordDialog = {
            viewModel.dismissUnauthorizedMessage()
            setUpdatePasswordDialogOpen(true)
        }
        val enableMarkReadOnScroll by appPreferences.articleListOptions.markReadOnScroll.collectChangesWithDefault()

        val listState = rememberSaveable(filter, saver = LazyListState.Saver) {
            LazyListState(0, 0)
        }

        fun scrollToArticle(index: Int) {
            scope.launch {
                if (index > -1) {
                    val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                    val isItemVisible = visibleItemsInfo.any { it.index == index }

                    if (!isItemVisible) {
                        listState.animateScrollToItem(index)
                    }
                }
            }
        }

        val resetScrollBehaviorOffset = resetScrollBehaviorListener(
            listState = listState,
            scrollBehavior = scrollBehavior
        )

        suspend fun openNextStatus(action: suspend () -> Unit) {
            action()
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }

        fun requestNextFeed() {
            scope.launchUI {
                openNextStatus {
                    viewModel.requestNextFeed()
                }
            }
        }

        fun markAllRead(range: MarkRead) {
            val animateMarkRead = openNextFeedOnReadAll &&
                    canSwipeToNextFeed &&
                    canOpenNextFeed(filter, range)

            if (animateMarkRead) {
                scope.launchUI {
                    openNextStatus {
                        onMarkAllRead(range)
                    }
                }
            } else {
                onMarkAllRead(range)
            }
        }

        val scrollToTop = {
            scope.launch {
                listState.scrollToItem(0)
                resetScrollBehaviorOffset()
            }
        }

        val refreshPagination = {
            scope.launch {
                resetScrollBehaviorOffset()
            }
        }

        fun initialize() {
            isRefreshing = true
            onInitialized {
                isRefreshing = false
                refreshPagination()
                if (!isRefreshInitialized) {
                    setRefreshInitialized(true)
                }
            }
        }

        fun refreshFeeds() {
            isRefreshing = true

            viewModel.refresh(filter) {
                isRefreshing = false
                refreshPagination()
            }
        }

        fun openNextList(action: suspend () -> Unit) {
            scope.launchUI {
                drawerState.close()
                openNextStatus(action)
            }
        }

        fun clearArticle() {
            scope.launchUI {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
            }
            viewModel.clearArticle()
        }

        val toggleDrawer = {
            scope.launch {
                if (drawerState.isOpen) {
                    drawerState.close()
                } else {
                    drawerState.open()
                }
            }
        }

        fun closeDrawer() {
            scope.launchUI {
                drawerState.close()
            }
        }

        fun openDrawer() {
            scope.launchUI {
                drawerState.open()
            }
        }

        val showSnackbar = { message: String ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }

        val onFeedAdded = { feedID: String ->
            scope.launch {
                openNextList { viewModel.selectFeed(feedID) }

                showSnackbar(addFeedSuccessMessage)
            }
        }

        fun selectArticle(articleID: String) {
            if (search.isActive) {
                focusManager.clearFocus()
            }

            scope.launchUI {
                viewModel.selectArticle(articleID)
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, articleID)
            }
        }

        val selectFilter = {
            if (!filter.hasArticlesSelected()) {
                openNextList { viewModel.selectArticleFilter() }
            } else {
                closeDrawer()
            }
        }

        val selectStatus = { status: ArticleStatus ->
            scope.launchUI {
                openNextStatus { viewModel.selectStatus(status) }
            }
        }

        val selectFeed = { feed: Feed, folderTitle: String? ->
            if (!filter.isFeedSelected(feed)) {
                openNextList { viewModel.selectFeed(feed.id, folderTitle) }
            } else {
                closeDrawer()
            }
        }

        val selectFolder = { folder: Folder ->
            if (!filter.isFolderSelected(folder)) {
                openNextList { viewModel.selectFolder(folder.title) }
            } else {
                closeDrawer()
            }
        }

        val selectSavedSearch = { savedSearch: SavedSearch ->
            if (!filter.isSavedSearchSelected(savedSearch)) {
                openNextList { viewModel.selectSavedSearch(savedSearch.id) }
            } else {
                closeDrawer()
            }
        }

        ArticleScaffold(
            drawerState = drawerState,
            scaffoldNavigator = scaffoldNavigator,
            drawerPane = {
                FeedList(
                    folders = folders,
                    feeds = feeds,
                    onSelectFolder = selectFolder,
                    onSelectFeed = selectFeed,
                    onFeedAdded = { onFeedAdded(it) },
                    savedSearches = savedSearches,
                    onSelectSavedSearch = selectSavedSearch,
                    onNavigateToSettings = onNavigateToSettings,
                    onFilterSelect = selectFilter,
                    onRefreshAll = { completion ->
                        viewModel.refreshAll(ArticleFilter.default()) {
                            if (enableMarkReadOnScroll) {
                                scrollToTop()
                            }
                            completion()
                        }
                    },
                    filter = filter,
                    statusCount = statusCount,
                    onSelectStatus = { selectStatus(it) }
                )
            },
            listPane = {
                val keyboardManager = LocalSoftwareKeyboardController.current

                Scaffold(
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .nestedScroll(object : NestedScrollConnection {
                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (search.isActive) {
                                    keyboardManager?.hide()
                                }

                                return Offset.Zero
                            }
                        }),
                    topBar = {
                        ArticleListTopBar(
                            onRequestJumpToTop = {
                                scrollToTop()
                            },
                            onNavigateToDrawer = {
                                openDrawer()
                            },
                            onRemoveFeed = { feedID, completion ->
                                viewModel.removeFeed(
                                    feedID,
                                    completion
                                )
                            },
                            onRemoveFolder = { folderTitle, completion ->
                                viewModel.removeFolder(
                                    folderTitle,
                                    completion
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            onMarkAllRead = {
                                markAllRead(it)
                            },
                            search = search,
                            filter = filter,
                            currentFeed = currentFeed,
                            feeds = allFeeds,
                            savedSearches = savedSearches,
                            folders = allFolders
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    floatingActionButton = {
                        MarkAllReadButton(
                            onMarkAllRead = {
                                markAllRead(MarkRead.All)
                            },
                        )
                    }
                ) { innerPadding ->
                    ArticleListScaffold(
                        padding = innerPadding,
                        showOnboarding = showOnboarding,
                        onboarding = {
                            EmptyOnboardingView {
                                AddFeedButton(
                                    onComplete = {
                                        onFeedAdded(it)
                                    }
                                )
                            }
                        },
                    ) {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                refreshFeeds()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            PullToNextFeedBox(
                                modifier = Modifier.fillMaxSize(),
                                enabled = canSwipeToNextFeed,
                                onRequestNext = {
                                    requestNextFeed()
                                },
                            ) {
                                key(filter) {
                                    ArticleList(
                                        articles = articles,
                                        selectedArticleKey = articleID,
                                        listState = listState,
                                        enableMarkReadOnScroll = enableMarkReadOnScroll,
                                        refreshingAll = viewModel.refreshingAll,
                                        onMarkAllRead = { range ->
                                            onMarkAllRead(range)
                                        },
                                        onSelect = { articleID ->
                                            selectArticle(articleID)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                if (articleID.isNullOrBlank() && showMultipleColumns) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CapyPlaceholder()
                    }
                } else if (articleID != null) {
                    ArticleView(
                        articleID = articleID,
                        onBackPressed = {
                            clearArticle()
                        },
                        enableBackHandler = media == null,
                        onSelectArticle = { index, articleID ->
                            selectArticle(articleID)
                            scrollToArticle(index)
                        },
                        onScrollToArticle = { index ->
                            scrollToArticle(index)
                        },
                        onNavigateToMedia = {
                            media = it
                        }
                    )
                }
            }
        )

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = media != null
        ) {
            ArticleMediaView(
                onDismissRequest = {
                    media = null
                },
                media = media
            )
        }

        if (viewModel.showUnauthorizedMessage) {
            UnauthorizedAlertDialog(
                onConfirm = openUpdatePasswordDialog,
                onDismissRequest = viewModel::dismissUnauthorizedMessage,
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
            if (!isRefreshInitialized) {
                initialize()
            }
        }

        BackHandler(media != null) {
            media = null
        }

        BackHandler(media == null && search.isActive && articleID.isNullOrBlank()) {
            search.clear()
        }

        ArticleListBackHandler(
            filter,
            onRequestFilter = selectFilter,
            onRequestFolder = selectFolder,
            enabled = isFeedActive(media, articleID, search),
            isDrawerOpen = drawerState.isOpen,
            toggleDrawer = {
                toggleDrawer()
            },
            closeDrawer = {
                closeDrawer()
            }
        )

        LayoutNavigationHandler(
            enabled = articleID.isNullOrBlank()
        ) {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }

        LaunchedEffect(filter) {
            resetScrollBehaviorOffset()
        }

        ArticleHandler(articleID) { id ->
            selectArticle(id)
        }
    }
}

@Composable
fun rememberArticleActions(viewModel: ArticleScreenViewModel): ArticleActions {
    return remember {
        ArticleActions(
            markRead = viewModel::markReadAsync,
            markUnread = viewModel::markUnreadAsync,
            star = viewModel::addStarAsync,
            unstar = viewModel::removeStarAsync,
        )
    }
}

@Composable
fun rememberFolderActions(viewModel: ArticleScreenViewModel): FolderActions {
    return remember {
        FolderActions(
            updateExpanded = viewModel::expandFolder,
        )
    }
}

fun canOpenNextFeed(
    filter: ArticleFilter,
    range: MarkRead,
): Boolean {
    return range is MarkRead.All && filter !is ArticleFilter.Articles
}

fun isFeedActive(
    media: Media?,
    articleID: String?,
    search: ArticleSearch
): Boolean {
    return media == null &&
            articleID.isNullOrBlank() &&
            !search.isActive
}

@Composable
fun rememberCurrentFeed(filter: ArticleFilter, feeds: List<Feed>): Feed? {
    return remember(filter, feeds) {
        if (filter is ArticleFilter.Feeds) {
            feeds.find { it.id == filter.feedID }
        } else {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberArticleTopBar(filter: ArticleFilter): TopAppBarScrollBehavior {
    val state = rememberSaveable(filter, saver = TopAppBarState.Saver) {
        TopAppBarState(
            initialHeightOffsetLimit = 0f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )
    }

    return pinnedScrollBehavior(state)
}
