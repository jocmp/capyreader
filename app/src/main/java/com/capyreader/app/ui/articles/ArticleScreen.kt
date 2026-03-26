package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.common.Media
import com.capyreader.app.common.Saver
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.LocalBadgeStyle
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
import com.capyreader.app.ui.articles.feeds.LocalSavedSearchActions
import com.capyreader.app.ui.articles.feeds.SavedSearchActions
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScreen(
    viewModel: ArticleScreenViewModel = koinViewModel(),
    feedListViewModel: FeedListViewModel = koinViewModel(),
    articleViewModel: ArticleViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject(),
    pendingArticleID: String? = null,
    onPendingArticleIDConsumed: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
) {
    val feeds by feedListViewModel.topLevelFeeds.collectAsStateWithLifecycle(emptyList())
    val pagesFeed by feedListViewModel.pagesFeed.collectAsStateWithLifecycle(null)
    val allFeeds by feedListViewModel.allFeeds.collectAsStateWithLifecycle(emptyList())
    val allFolders by feedListViewModel.allFolders.collectAsStateWithLifecycle(emptyList())
    val folders by feedListViewModel.folders.collectAsStateWithLifecycle(emptyList())
    val savedSearches by feedListViewModel.savedSearches.collectAsStateWithLifecycle(emptyList())
    val allSavedSearches by feedListViewModel.allSavedSearches.collectAsStateWithLifecycle(emptyList())
    val statusCount by feedListViewModel.statusCount.collectAsStateWithLifecycle(0)
    val todayCount by feedListViewModel.todayCount.collectAsStateWithLifecycle(0)
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle(0L)
    val showTodayFilter by feedListViewModel.showTodayFilter.collectAsStateWithLifecycle(true)
    val showOnboarding by feedListViewModel.showOnboarding.collectAsState(false)

    val filter by viewModel.filter.collectAsStateWithLifecycle(appPreferences.filter.get())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle("")
    val searchState by viewModel.searchState.collectAsStateWithLifecycle(SearchState.INACTIVE)
    val nextFilter by viewModel.nextFilter.collectAsStateWithLifecycle(null)
    val afterReadAll by viewModel.afterReadAll.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val refreshInterval by appPreferences.refreshInterval.collectChangesWithDefault(appPreferences.refreshInterval.get())

    val canSwipeToNextFeed = nextFilter != null
    val context = LocalContext.current

    val folderActions = rememberFolderActions(feedListViewModel, viewModel)
    val feedActions = rememberFeedActions(feedListViewModel, viewModel)
    val savedSearchActions = rememberSavedSearchActions(feedListViewModel)
    val connectivity = rememberLocalConnectivity()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val markAllReadButtonPosition by appPreferences.articleListOptions.markReadButtonPosition.collectChangesWithCurrent()
    val badgeStyle by appPreferences.badgeStyle.collectChangesWithDefault()

    val articles = viewModel.articles.collectAsLazyPagingItems()

    val backStack = rememberNavBackStack(ArticleNavKey.List)
    val article = articleViewModel.article
    val selectedArticleId = article?.id

    val snackbarHostState = remember { SnackbarHostState() }
    val linkOpener = provideLinkOpener(context)
    val audioController: AudioPlayerController = koinInject()
    val audioEnclosure by audioController.currentAudio.collectAsState()

    val skipInitialRefresh = refreshInterval != RefreshInterval.ON_START
    val (isRefreshInitialized, setRefreshInitialized) = rememberSaveable { mutableStateOf(skipInitialRefresh) }
    var refreshAllState by remember { mutableStateOf(AngleRefreshState.STOPPED) }
    val (isUpdatePasswordDialogOpen, setUpdatePasswordDialogOpen) = rememberSaveable { mutableStateOf(false) }
    var isPullToRefreshing by remember { mutableStateOf(false) }
    val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
    val currentFeed = remember(allFeeds, filter) {
        if (filter is ArticleFilter.Feeds) allFeeds.find { it.id == (filter as ArticleFilter.Feeds).feedID } else null
    }
    val scrollBehavior = pinnedScrollBehavior()
    var media by rememberSaveable(saver = Media.Saver) { mutableStateOf(null) }
    val enableMarkReadOnScroll by appPreferences.articleListOptions.markReadOnScroll.collectChangesWithDefault()

    val listState = articles.rememberLazyListState()

    val search = ArticleSearch(
        query = searchQuery,
        start = { viewModel.startSearch() },
        clear = { viewModel.clearSearch() },
        update = viewModel::updateSearch,
        state = searchState,
    )

    // Clear article selection when filter changes (skip initial composition)
    val previousFilter = rememberSaveable(saver = ArticleFilter.Saver) { mutableStateOf(filter) }
    LaunchedEffect(filter) {
        if (filter != previousFilter.value) {
            previousFilter.value = filter
            articleViewModel.clearArticle()
            backStack.removeAll { it is ArticleNavKey.Detail }
        }
    }

    // Handle notification / widget deep link
    LaunchedEffect(pendingArticleID) {
        val id = pendingArticleID ?: return@LaunchedEffect
        onPendingArticleIDConsumed()
        articleViewModel.loadArticle(id) { loaded ->
            if (loaded.openInBrowser && loaded.url != null) {
                linkOpener.open(loaded.url.toString().toUri())
            } else if (backStack.lastOrNull() !is ArticleNavKey.Detail) {
                backStack.add(ArticleNavKey.Detail)
            }
        }
    }

    val resetScrollBehaviorOffset = resetScrollBehaviorListener(listState = listState, scrollBehavior = scrollBehavior)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .drop(if (enableMarkReadOnScroll) 0 else 1)
            .distinctUntilChanged()
            .collect {
                listState.scrollToItem(0)
                resetScrollBehaviorOffset()
            }
    }

    val (scrolledFilter, setScrolledFilter) = rememberSaveable(saver = ArticleFilter.Saver) { mutableStateOf(null) }

    LaunchedEffect(filter, articles.loadState.refresh) {
        val refreshComplete = articles.loadState.refresh is LoadState.NotLoading
        if (refreshComplete && filter != scrolledFilter) {
            listState.scrollToItem(0)
            resetScrollBehaviorOffset()
            setScrolledFilter(filter)
        }
    }

    val scrollToTop = {
        scope.launch {
            listState.scrollToItem(0)
            resetScrollBehaviorOffset()
        }
    }

    fun scrollToArticle(index: Int) {
        scope.launch {
            if (index > -1) {
                val isItemVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == index }
                if (!isItemVisible) listState.animateScrollToItem(index)
            }
        }
    }

    fun refreshAll() {
        if (enableMarkReadOnScroll) scrollToTop()
        if (refreshAllState == AngleRefreshState.RUNNING) return
        refreshAllState = AngleRefreshState.RUNNING
        viewModel.refreshAll {
            refreshAllState = AngleRefreshState.SETTLING
            scope.launch { resetScrollBehaviorOffset() }
            if (!isRefreshInitialized) setRefreshInitialized(true)
        }
    }

    fun refreshFeeds() {
        isPullToRefreshing = true
        viewModel.refresh(filter) {
            isPullToRefreshing = false
            scope.launch { resetScrollBehaviorOffset() }
        }
    }

    fun requestNextFeed() {
        scope.launchUI {
            articleViewModel.clearArticle()
            backStack.removeAll { it is ArticleNavKey.Detail }
            delay(300)
            viewModel.requestNextFeed()
        }
    }

    fun openNextFeed() {
        scope.launchIO { viewModel.requestNextFeed() }
        articleViewModel.clearArticle()
        backStack.removeAll { it is ArticleNavKey.Detail }
    }

    fun markAllRead(range: MarkRead) {
        val animateMarkRead = afterReadAll == AfterReadAllBehavior.OPEN_NEXT_FEED &&
                canSwipeToNextFeed &&
                canOpenNextFeed(filter, range)

        if (animateMarkRead) {
            openNextFeed()
        }

        viewModel.markAllRead(
            onArticlesCleared = { scope.launchUI { drawerState.open() } },
            searches = savedSearches,
            folders = folders,
            feeds = feeds,
            range = range,
        )
    }

    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message, withDismissAction = true, duration = SnackbarDuration.Short)
        }
    }

    fun openDrawer() { scope.launchUI { drawerState.open() } }
    fun closeDrawer() { scope.launchUI { drawerState.close() } }

    fun openNextList(action: suspend () -> Unit) {
        scope.launchUI {
            drawerState.close()
            delay(300)
            backStack.removeAll { it is ArticleNavKey.Detail }
            action()
        }
    }

    val selectFilter = {
        if (!filter.hasArticlesSelected()) openNextList { viewModel.selectArticleFilter() }
        else closeDrawer()
    }

    val selectStatus = { status: ArticleStatus ->
        scope.launchUI { backStack.removeAll { it is ArticleNavKey.Detail }; viewModel.selectStatus(status) }
    }

    val selectFeed = { feed: Feed, folderTitle: String? ->
        if (!filter.isFeedSelected(feed)) openNextList { viewModel.selectFeed(feed.id, folderTitle) }
        else closeDrawer()
    }

    val selectFolder = { folder: Folder ->
        if (!filter.isFolderSelected(folder)) openNextList { viewModel.selectFolder(folder.title) }
        else closeDrawer()
    }

    val selectSavedSearch = { savedSearch: SavedSearch ->
        if (!filter.isSavedSearchSelected(savedSearch)) openNextList { viewModel.selectSavedSearch(savedSearch.id) }
        else closeDrawer()
    }

    val selectToday = {
        if (!filter.hasTodaySelected()) openNextList { viewModel.selectToday() }
        else closeDrawer()
    }

    val onFeedAdded = { feedID: String ->
        scope.launch {
            openNextList { viewModel.selectFeed(feedID) }
            showSnackbar(addFeedSuccessMessage)
        }
    }

    val labelsActions = rememberLabelsActions(viewModel, allSavedSearches)
    val openUpdatePasswordDialog = {
        viewModel.dismissUnauthorizedMessage()
        setUpdatePasswordDialogOpen(true)
    }

    CompositionLocalProvider(
        LocalArticleActions provides rememberArticleActions(viewModel),
        LocalFolderActions provides folderActions,
        LocalFeedActions provides feedActions,
        LocalSavedSearchActions provides savedSearchActions,
        LocalLabelsActions provides labelsActions,
        LocalConnectivity provides connectivity,
        LocalLinkOpener provides linkOpener,
        LocalMarkAllReadButtonPosition provides markAllReadButtonPosition,
        LocalBadgeStyle provides badgeStyle,
        LocalUnreadCount provides unreadCount,
        LocalSnackbarHost provides snackbarHostState,
    ) {
        ArticleScaffold(
            drawerState = drawerState,
            isDetailVisible = selectedArticleId != null,
            drawerPane = {
                FeedList(
                    source = feedListViewModel.source,
                    folders = folders,
                    feeds = feeds,
                    pagesFeed = pagesFeed,
                    onSelectFolder = selectFolder,
                    onSelectFeed = selectFeed,
                    onFeedAdded = { onFeedAdded(it) },
                    savedSearches = savedSearches,
                    onSelectSavedSearch = selectSavedSearch,
                    onNavigateToSettings = {
                        onNavigateToSettings()
                        scope.launchUI { delay(100); drawerState.close() }
                    },
                    onFilterSelect = selectFilter,
                    onSelectToday = { selectToday() },
                    refreshState = refreshAllState,
                    onRefresh = { refreshAll() },
                    filter = filter,
                    statusCount = statusCount,
                    todayCount = todayCount,
                    showTodayFilter = showTodayFilter,
                    onSelectStatus = { selectStatus(it) }
                )
            },
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = rememberListDetailSceneStrategy(),
                entryProvider = entryProvider {
                    entry<ArticleNavKey.List>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { CapyPlaceholder() }
                        )
                    ) {
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            topBar = {
                                ArticleListTopBar(
                                    onRequestJumpToTop = { scrollToTop() },
                                    onNavigateToDrawer = { openDrawer() },
                                    onRemoveFolder = { folderTitle, completion ->
                                        feedListViewModel.removeFolder(folderTitle, completion)
                                    },
                                    scrollBehavior = scrollBehavior,
                                    onMarkAllRead = { markAllRead(MarkRead.All) },
                                    search = search,
                                    filter = filter,
                                    currentFeed = currentFeed,
                                    feeds = allFeeds,
                                    savedSearches = savedSearches,
                                    folders = allFolders,
                                    source = viewModel.source,
                                )
                            },
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            floatingActionButton = {
                                if (markAllReadButtonPosition == MarkReadPosition.FLOATING_ACTION_BUTTON) {
                                    MarkAllReadButton(
                                        onMarkAllRead = { markAllRead(MarkRead.All) },
                                        position = MarkReadPosition.FLOATING_ACTION_BUTTON,
                                    )
                                }
                            },
                            bottomBar = {
                                audioEnclosure?.let { audio ->
                                    FloatingAudioPlayer(
                                        audio = audio,
                                        controller = audioController,
                                        onDismiss = { audioController.dismiss() },
                                    )
                                }
                            }
                        ) { innerPadding ->
                            ArticleListScaffold(
                                padding = innerPadding,
                                showOnboarding = showOnboarding,
                                onboarding = {
                                    EmptyOnboardingView {
                                        AddFeedButton(onComplete = { onFeedAdded(it) })
                                    }
                                },
                            ) {
                                PullToRefreshBox(
                                    isRefreshing = isPullToRefreshing,
                                    onRefresh = { refreshFeeds() },
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    PullToNextFeedBox(
                                        modifier = Modifier.fillMaxSize(),
                                        enabled = canSwipeToNextFeed,
                                        onRequestNext = { requestNextFeed() },
                                    ) {
                                        if (isRefreshInitialized && articles.itemCount == 0) {
                                            ArticleListEmptyView()
                                        } else {
                                            ArticleList(
                                                articles = articles,
                                                selectedArticleKey = selectedArticleId,
                                                listState = listState,
                                                enableMarkReadOnScroll = enableMarkReadOnScroll,
                                                refreshingAll = viewModel.refreshingAll,
                                                filterStatus = filter.status,
                                                onMarkAllRead = { range -> markAllRead(range) },
                                                onSelect = { articleID ->
                                                    articleViewModel.loadArticle(articleID) { loaded ->
                                                        if (loaded.openInBrowser && loaded.url != null) {
                                                            linkOpener.open(loaded.url.toString().toUri())
                                                        } else if (backStack.lastOrNull() !is ArticleNavKey.Detail) {
                                                            backStack.add(ArticleNavKey.Detail)
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    entry<ArticleNavKey.Detail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        CompositionLocalProvider(
                            LocalFullContent provides FullContentFetcher(
                                fetch = { articleViewModel.fetchFullContentAsync() },
                                reset = articleViewModel::resetFullContent,
                            )
                        ) {
                            if (article != null) {
                                val isAudioPlaying by audioController.isPlaying.collectAsState()
                                val currentAudio by audioController.currentAudio.collectAsState()

                                ArticleView(
                                    article = article,
                                    articles = articles,
                                    onBackPressed = {
                                        articleViewModel.clearArticle()
                                        backStack.removeLastOrNull()
                                    },
                                    onToggleRead = articleViewModel::toggleArticleRead,
                                    onToggleStar = articleViewModel::toggleArticleStar,
                                    canSaveExternally = articleViewModel.canSaveArticleExternally.collectAsStateWithLifecycle().value,
                                    onDeletePage = {
                                        val id = article.id
                                        articleViewModel.clearArticle()
                                        backStack.removeLastOrNull()
                                        articleViewModel.deletePage(id)
                                    },
                                    onSelectMedia = { media = it },
                                    onSelectAudio = { audio -> audioController.play(audio) },
                                    onPauseAudio = { audioController.pause() },
                                    onSelectArticle = { articleID ->
                                        articleViewModel.loadArticle(articleID) { loaded ->
                                            if (loaded.openInBrowser && loaded.url != null) {
                                                articleViewModel.clearArticle()
                                                backStack.removeLastOrNull()
                                                linkOpener.open(loaded.url.toString().toUri())
                                            }
                                        }
                                    },
                                    onScrollToArticle = { index -> scrollToArticle(index) },
                                    currentAudioUrl = currentAudio?.url,
                                    isAudioPlaying = isAudioPlaying,
                                    isFullscreen = false,
                                    onToggleFullscreen = {},
                                )
                            }
                        }
                    }
                }
            )
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = media != null,
        ) {
            ArticleMediaView(
                onDismissRequest = { media = null },
                media = media
            )
        }

        BackHandler(media != null) {
            media = null
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
                onDismissRequest = { setUpdatePasswordDialogOpen(false) }
            )
        }

        labelsActions.selectedArticleID?.let { articleID ->
            LabelBottomSheet(
                articleID = articleID,
                savedSearches = labelsActions.savedSearches,
                articleLabels = labelsActions.articleLabels,
                onAddLabel = { savedSearchID -> labelsActions.addLabel(articleID, savedSearchID) },
                onRemoveLabel = { savedSearchID -> labelsActions.removeLabel(articleID, savedSearchID) },
                onCreateLabel = labelsActions.createLabel,
                onDismissRequest = labelsActions.closeSheet
            )
        }

        LaunchedEffect(Unit) {
            if (!isRefreshInitialized) refreshAll()
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
            saveExternally = viewModel::saveArticleExternallyAsync,
        )
    }
}

@Composable
fun rememberFolderActions(
    feedListViewModel: FeedListViewModel,
    viewModel: ArticleScreenViewModel,
): FolderActions {
    return remember {
        FolderActions(
            updateExpanded = feedListViewModel::expandFolder,
            removeFolder = { folderTitle, completion ->
                feedListViewModel.removeFolder(folderTitle) { result ->
                    result.onSuccess { viewModel.resetToDefaultFilter() }
                    completion(result)
                }
            },
        )
    }
}

@Composable
fun rememberFeedActions(
    feedListViewModel: FeedListViewModel,
    viewModel: ArticleScreenViewModel,
): FeedActions {
    return remember {
        FeedActions(
            updateOpenInBrowser = { feedID, openInBrowser -> feedListViewModel.updateOpenInBrowser(feedID, openInBrowser) },
            removeFeed = { feedID -> feedListViewModel.removeFeed(feedID) { viewModel.resetToDefaultFilter() } },
            toggleUnreadBadge = { feedID, show -> feedListViewModel.toggleFeedUnreadBadge(feedID, show) },
            reloadIcon = { feedID -> feedListViewModel.reloadFavicon(feedID) }
        )
    }
}

@Composable
fun rememberLabelsActions(
    viewModel: ArticleScreenViewModel,
    savedSearches: List<SavedSearch>,
): LabelsActions {
    var selectedArticleID by remember { mutableStateOf<String?>(null) }
    val articleLabels by viewModel.getArticleLabels(selectedArticleID).collectAsState(initial = emptyList())

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
fun rememberSavedSearchActions(viewModel: FeedListViewModel): SavedSearchActions {
    return remember {
        SavedSearchActions(
            toggleUnreadBadge = { id, show -> viewModel.toggleSavedSearchUnreadBadge(id, show) },
        )
    }
}

fun canOpenNextFeed(filter: ArticleFilter, range: MarkRead): Boolean {
    return range is MarkRead.All && filter !is ArticleFilter.Articles
}
