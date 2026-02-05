package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.common.Media
import com.capyreader.app.common.Saver
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.LocalMarkAllReadButtonPosition
import com.capyreader.app.ui.LocalUnreadCount
import com.capyreader.app.ui.articles.audio.AudioPlayerController
import com.capyreader.app.ui.articles.audio.FloatingAudioPlayer
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.feeds.AngleRefreshState
import com.capyreader.app.ui.articles.feeds.FeedActions
import com.capyreader.app.ui.articles.feeds.FeedList
import com.capyreader.app.ui.articles.feeds.FolderActions
import com.capyreader.app.ui.articles.feeds.LocalFeedActions
import com.capyreader.app.ui.articles.feeds.LocalFolderActions
import com.capyreader.app.ui.articles.list.ArticleListTopBar
import com.capyreader.app.ui.articles.list.EmptyOnboardingView
import com.capyreader.app.ui.articles.list.LabelBottomSheet
import com.capyreader.app.ui.articles.list.MarkAllReadButton
import com.capyreader.app.ui.articles.list.PullToNextFeedBox
import com.capyreader.app.ui.articles.list.resetScrollBehaviorListener
import com.capyreader.app.ui.articles.media.ArticleMediaView
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchState
import com.capyreader.app.ui.provideLinkOpener
import com.capyreader.app.ui.rememberLazyListState
import com.capyreader.app.ui.rememberLocalConnectivity
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
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
    val feeds by viewModel.topLevelFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFeeds by viewModel.allFeeds.collectAsStateWithLifecycle(initialValue = emptyList())
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val statusCount by viewModel.statusCount.collectAsStateWithLifecycle(initialValue = 0)
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle(initialValue = 0)
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle(initialValue = 0L)
    val showTodayFilter by viewModel.showTodayFilter.collectAsStateWithLifecycle(initialValue = true)
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
    val context = LocalContext.current

    val fullContent = rememberFullContent(viewModel)
    val articleActions = rememberArticleActions(viewModel)
    val folderActions = rememberFolderActions(viewModel)
    val feedActions = rememberFeedActions(viewModel)
    val labelsActions = rememberLabelsActions(viewModel, savedSearches)
    val connectivity = rememberLocalConnectivity()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val showOnboarding by viewModel.showOnboarding.collectAsState(false)
    val markAllReadButtonPosition by appPreferences
        .articleListOptions
        .markReadButtonPosition
        .collectChangesWithCurrent()

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

    val article = viewModel.article

    val search = ArticleSearch(
        query = searchQuery,
        start = { viewModel.startSearch() },
        clear = { viewModel.clearSearch() },
        update = viewModel::updateSearch,
        state = searchState,
    )

    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalFullContent provides fullContent,
        LocalArticleActions provides articleActions,
        LocalFolderActions provides folderActions,
        LocalFeedActions provides feedActions,
        LocalLabelsActions provides labelsActions,
        LocalConnectivity provides connectivity,
        LocalLinkOpener provides provideLinkOpener(context),
        LocalMarkAllReadButtonPosition provides markAllReadButtonPosition,
        LocalUnreadCount provides unreadCount,
        LocalSnackbarHost provides snackbarHostState,
    ) {
        val openNextFeedOnReadAll = afterReadAll == AfterReadAllBehavior.OPEN_NEXT_FEED

        val skipInitialRefresh = refreshInterval != RefreshInterval.ON_START

        val (isRefreshInitialized, setRefreshInitialized) = rememberSaveable {
            mutableStateOf(skipInitialRefresh)
        }
        var refreshAllState by remember { mutableStateOf(AngleRefreshState.STOPPED) }

        val (isUpdatePasswordDialogOpen, setUpdatePasswordDialogOpen) = rememberSaveable {
            mutableStateOf(false)
        }
        val coroutineScope = rememberCoroutineScope()
        val scaffoldNavigator = rememberArticleScaffoldNavigator()
        val showMultipleColumns = scaffoldNavigator.scaffoldDirective.maxHorizontalPartitions > 1
        var isPullToRefreshing by remember { mutableStateOf(false) }
        val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
        val currentFeed by viewModel.currentFeed.collectAsStateWithLifecycle(null)
        val scrollBehavior = pinnedScrollBehavior()
        var media by rememberSaveable(saver = Media.Saver) { mutableStateOf(null) }
        val audioController: AudioPlayerController = koinInject()
        val audioEnclosure by audioController.currentAudio.collectAsState()
        val focusManager = LocalFocusManager.current
        val openUpdatePasswordDialog = {
            viewModel.dismissUnauthorizedMessage()
            setUpdatePasswordDialogOpen(true)
        }
        val enableMarkReadOnScroll by appPreferences.articleListOptions.markReadOnScroll.collectChangesWithDefault()

        suspend fun navigateToDetail() {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        }

        val listState = articles.rememberLazyListState()

        fun scrollToArticle(index: Int) {
            coroutineScope.launch {
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

        val scrollToTop = {
            coroutineScope.launch {
                listState.scrollToItem(0)
                resetScrollBehaviorOffset()
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .drop(if (enableMarkReadOnScroll) 0 else 1)
                .distinctUntilChanged()
                .collect {
                    listState.scrollToItem(0)
                    resetScrollBehaviorOffset()
                }
        }

        val (scrolledFilter, setScrolledFilter) = remember { mutableStateOf<ArticleFilter?>(null) }

        LaunchedEffect(filter, articles.loadState.refresh) {
            val refreshComplete = articles.loadState.refresh is LoadState.NotLoading
            if (refreshComplete && filter != scrolledFilter) {
                listState.scrollToItem(0)
                resetScrollBehaviorOffset()
                setScrolledFilter(filter)
            }
        }

        suspend fun openNextStatus(action: suspend () -> Unit) {
            scope.launchIO { action() }
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }

        fun requestNextFeed() {
            coroutineScope.launchUI {
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
                coroutineScope.launchUI {
                    openNextStatus {
                        onMarkAllRead(range)
                    }
                }
            } else {
                onMarkAllRead(range)
            }
        }

        val refreshPagination = {
            coroutineScope.launch {
                resetScrollBehaviorOffset()
            }
        }

        fun refreshAll() {
            if (enableMarkReadOnScroll) {
                scrollToTop()
            }

            if (refreshAllState == AngleRefreshState.RUNNING) {
                return
            }


            refreshAllState = AngleRefreshState.RUNNING

            viewModel.refreshAll {
                refreshAllState = AngleRefreshState.SETTLING
                refreshPagination()

                if (!isRefreshInitialized) {
                    setRefreshInitialized(true)
                }
            }
        }

        fun refreshFeeds() {
            isPullToRefreshing = true

            viewModel.refresh(filter) {
                isPullToRefreshing = false
                refreshPagination()
            }
        }

        fun openNextList(action: suspend () -> Unit) {
            coroutineScope.launchUI {
                drawerState.close()
                delay(300)
                openNextStatus(action)
            }
        }

        fun clearArticle() {
            coroutineScope.launchUI {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
            }
            viewModel.clearArticle()
        }

        val toggleDrawer = {
            coroutineScope.launch {
                if (drawerState.isOpen) {
                    drawerState.close()
                } else {
                    drawerState.open()
                }
            }
        }

        fun closeDrawer() {
            coroutineScope.launchUI {
                drawerState.close()
            }
        }

        fun openDrawer() {
            coroutineScope.launchUI {
                drawerState.open()
            }
        }

        val showSnackbar = { message: String ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }

        val onFeedAdded = { feedID: String ->
            coroutineScope.launch {
                openNextList { viewModel.selectFeed(feedID) }

                showSnackbar(addFeedSuccessMessage)
            }
        }

        fun setArticle(articleID: String, onComplete: (article: Article) -> Unit = {}) {
            viewModel.selectArticle(articleID, onComplete)
        }

        val linkOpener = LocalLinkOpener.current

        fun selectArticle(articleID: String) {
            setArticle(articleID) { nextArticle ->
                if (search.isActive) {
                    focusManager.clearFocus()
                }

                val url = nextArticle.url
                if (nextArticle.openInBrowser && url != null) {
                    clearArticle()
                    linkOpener.open(url.toString().toUri())
                } else {
                    coroutineScope.launch {
                        navigateToDetail()
                    }
                }
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
            coroutineScope.launchUI {
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

        val selectToday = {
            if (!filter.hasTodaySelected()) {
                openNextList { viewModel.selectToday() }
            } else {
                closeDrawer()
            }
        }

        ArticleHandler(article) { articleID ->
            selectArticle(articleID)
        }

        ArticleScaffold(
            drawerState = drawerState,
            scaffoldNavigator = scaffoldNavigator,
            drawerPane = {
                FeedList(
                    source = viewModel.source,
                    folders = folders,
                    feeds = feeds,
                    onSelectFolder = selectFolder,
                    onSelectFeed = selectFeed,
                    onFeedAdded = { onFeedAdded(it) },
                    savedSearches = savedSearches,
                    onSelectSavedSearch = selectSavedSearch,
                    onNavigateToSettings = {
                        onNavigateToSettings()
                        coroutineScope.launchUI {
                            delay(100)
                            drawerState.close()
                        }
                    },
                    onFilterSelect = selectFilter,
                    onSelectToday = { selectToday() },
                    refreshState = refreshAllState,
                    onRefresh = {
                        refreshAll()
                    },
                    filter = filter,
                    statusCount = statusCount,
                    todayCount = todayCount,
                    showTodayFilter = showTodayFilter,
                    onSelectStatus = { selectStatus(it) }
                )
            },
            listPane = {
                val keyboardManager = LocalSoftwareKeyboardController.current
                val markReadPosition = LocalMarkAllReadButtonPosition.current

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
                            onRemoveFolder = { folderTitle, completion ->
                                viewModel.removeFolder(
                                    folderTitle,
                                    completion
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            onMarkAllRead = {
                                markAllRead(MarkRead.All)
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
                        if (markReadPosition == MarkReadPosition.FLOATING_ACTION_BUTTON) {
                            MarkAllReadButton(
                                onMarkAllRead = {
                                    markAllRead(MarkRead.All)
                                },
                                position = MarkReadPosition.FLOATING_ACTION_BUTTON,
                            )
                        }
                    },
                    bottomBar = {
                        audioEnclosure?.let { audio ->
                            FloatingAudioPlayer(
                                audio = audio,
                                controller = audioController,
                                onDismiss = {
                                    audioController.dismiss()
                                },
                            )
                        }
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
                            isRefreshing = isPullToRefreshing,
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
                                if (isRefreshInitialized && articles.itemCount == 0) {
                                    ArticleListEmptyView()
                                } else {
                                    ArticleList(
                                        articles = articles,
                                        selectedArticleKey = article?.id,
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
                if (article == null && showMultipleColumns) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CapyPlaceholder()
                    }
                } else if (article != null) {
                    val isAudioPlaying by audioController.isPlaying.collectAsState()
                    val currentAudio by audioController.currentAudio.collectAsState()

                    ArticleView(
                        article = article,
                        articles = articles,
                        onBackPressed = {
                            clearArticle()
                        },
                        onToggleRead = viewModel::toggleArticleRead,
                        onToggleStar = viewModel::toggleArticleStar,
                        enableBackHandler = media == null,
                        onSelectMedia = { media = it },
                        onSelectAudio = { audio ->
                            audioController.play(audio)
                        },
                        onPauseAudio = {
                            audioController.pause()
                        },
                        onSelectArticle = { articleID ->
                            setArticle(articleID)
                        },
                        onScrollToArticle = { index ->
                            scrollToArticle(index)
                        },
                        currentAudioUrl = currentAudio?.url,
                        isAudioPlaying = isAudioPlaying,
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

        labelsActions.selectedArticleID?.let { articleID ->
            LabelBottomSheet(
                articleID = articleID,
                savedSearches = labelsActions.savedSearches,
                articleLabels = labelsActions.articleLabels,
                onAddLabel = { savedSearchID ->
                    labelsActions.addLabel(articleID, savedSearchID)
                },
                onRemoveLabel = { savedSearchID ->
                    labelsActions.removeLabel(articleID, savedSearchID)
                },
                onCreateLabel = labelsActions.createLabel,
                onDismissRequest = labelsActions.closeSheet
            )
        }

        LaunchedEffect(Unit) {
            if (!isRefreshInitialized) {
                refreshAll()
            }
        }

        BackHandler(media != null) {
            media = null
        }

        BackHandler(media == null && search.isActive && article == null) {
            search.clear()
        }

        ArticleListBackHandler(
            filter,
            onRequestFilter = selectFilter,
            onRequestFolder = selectFolder,
            enabled = isFeedActive(media, article, search),
            isDrawerOpen = drawerState.isOpen,
            toggleDrawer = {
                toggleDrawer()
            },
            closeDrawer = {
                closeDrawer()
            }
        )

        LayoutNavigationHandler(
            enabled = article == null
        ) {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
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

@Composable
fun rememberFeedActions(viewModel: ArticleScreenViewModel): FeedActions {
    return remember {
        FeedActions(
            updateOpenInBrowser = { feedID, openInBrowser ->
                viewModel.updateOpenInBrowser(feedID, openInBrowser)
            },
            removeFeed = { feedID ->
                viewModel.removeFeed(feedID)
            }
        )
    }
}

@Composable
fun rememberLabelsActions(
    viewModel: ArticleScreenViewModel,
    savedSearches: List<SavedSearch>,
): LabelsActions {
    var selectedArticleID by remember { mutableStateOf<String?>(null) }

    val articleLabels by viewModel.getArticleLabels(selectedArticleID)
        .collectAsState(initial = emptyList())

    return remember(savedSearches, selectedArticleID, articleLabels) {
        LabelsActions(
            source = viewModel.source,
            showLabels = viewModel.source.supportsLabels,
            savedSearches = savedSearches,
            selectedArticleID = selectedArticleID,
            articleLabels = articleLabels,
            openSheet = { selectedArticleID = it },
            closeSheet = { selectedArticleID = null },
            addLabel = viewModel::addLabelAsync,
            removeLabel = viewModel::removeLabelAsync,
            createLabel = viewModel::createLabel,
        )
    }
}

@Composable
fun rememberFullContent(viewModel: ArticleScreenViewModel): FullContentFetcher {
    return remember {
        FullContentFetcher(
            fetch = viewModel::fetchFullContentAsync,
            reset = viewModel::resetFullContent,
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
    article: Article?,
    search: ArticleSearch
): Boolean {
    return media == null &&
            article == null &&
            !search.isActive
}
