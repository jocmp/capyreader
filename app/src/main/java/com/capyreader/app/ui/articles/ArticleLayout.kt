package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.Media
import com.capyreader.app.common.Saver
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.list.EmptyOnboardingView
import com.capyreader.app.ui.articles.list.FeedListTopBar
import com.capyreader.app.ui.articles.list.PullToNextFeedBox
import com.capyreader.app.ui.articles.list.resetScrollBehaviorListener
import com.capyreader.app.ui.articles.media.ArticleMediaView
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.ArticleSearch
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ArticleLayout(
    filter: ArticleFilter,
    folders: List<Folder>,
    feeds: List<Feed>,
    savedSearches: List<SavedSearch>,
    allFeeds: List<Feed>,
    allFolders: List<Folder>,
    articles: LazyPagingItems<Article>,
    article: Article?,
    search: ArticleSearch,
    statusCount: Long,
    refreshInterval: RefreshInterval,
    onInitialized: (completion: () -> Unit) -> Unit,
    onFeedRefresh: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectSavedSearch: (savedSearchID: String) -> Unit,
    onSelectFeed: (feedID: String, folderTitle: String?) -> Unit,
    onSelectArticleFilter: () -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onSelectArticle: (articleID: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestClearArticle: () -> Unit,
    onToggleArticleRead: () -> Unit,
    onToggleArticleStar: () -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    onRequestNextFeed: () -> Unit,
    onRemoveFeed: (feedID: String, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    drawerState: DrawerState,
    showUnauthorizedMessage: Boolean,
    onUnauthorizedDismissRequest: () -> Unit,
    canSwipeToNextFeed: Boolean,
    openNextFeedOnReadAll: Boolean,
    appPreferences: AppPreferences = koinInject()
) {
    val skipInitialRefresh = refreshInterval == RefreshInterval.MANUALLY_ONLY

    val (isInitialized, setInitialized) = rememberSaveable {
        mutableStateOf(skipInitialRefresh)
    }
    val (isUpdatePasswordDialogOpen, setUpdatePasswordDialogOpen) = rememberSaveable {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldNavigator = rememberArticleScaffoldNavigator()
    val hasMultipleColumns = scaffoldNavigator.scaffoldDirective.maxHorizontalPartitions > 1
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHost = remember { SnackbarHostState() }
    val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
    val currentFeed = findCurrentFeed(filter, allFeeds)
    val scrollBehavior = rememberArticleTopBar(filter)
    var media by rememberSaveable(saver = Media.Saver) { mutableStateOf(null) }
    val focusManager = LocalFocusManager.current
    val openUpdatePasswordDialog = {
        onUnauthorizedDismissRequest()
        setUpdatePasswordDialogOpen(true)
    }
    val enableMarkReadOnScroll by appPreferences.articleListOptions.markReadOnScroll.collectChangesWithDefault()

    suspend fun navigateToDetail() {
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

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

    suspend fun openNextStatus(action: suspend () -> Unit) {
        action()
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
    }

    fun requestNextFeed() {
        coroutineScope.launchUI {
            openNextStatus {
                onRequestNextFeed()
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

    val scrollToTop = {
        coroutineScope.launch {
            listState.scrollToItem(0)
            resetScrollBehaviorOffset()
        }
    }

    val refreshPagination = {
        coroutineScope.launch {
            resetScrollBehaviorOffset()
        }
    }

    fun initialize() {
        isRefreshing = true
        onInitialized {
            isRefreshing = false
            refreshPagination()
            if (!isInitialized) {
                setInitialized(true)
            }
        }
    }

    fun refreshFeeds() {
        isRefreshing = true
        onFeedRefresh {
            isRefreshing = false
            refreshPagination()
        }
    }

    fun openNextList(action: suspend () -> Unit) {
        coroutineScope.launchUI {
            drawerState.close()
            openNextStatus(action)
        }
    }

    fun clearArticle() {
        coroutineScope.launchUI {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
        onRequestClearArticle()
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
            snackbarHost.showSnackbar(
                message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    val onFeedAdded = { feedID: String ->
        coroutineScope.launch {
            openNextList { onSelectFeed(feedID, null) }

            showSnackbar(addFeedSuccessMessage)
        }
    }

    fun selectArticle(articleID: String) {
        onSelectArticle(articleID)
        if (search.isActive) {
            focusManager.clearFocus()
        }
        coroutineScope.launch {
            navigateToDetail()
        }
    }

    fun selectFilter() {
        if (!filter.hasArticlesSelected()) {
            openNextList { onSelectArticleFilter() }
        } else {
            closeDrawer()
        }
    }

    fun selectStatus(status: ArticleStatus) {
        coroutineScope.launchUI {
            openNextStatus { onSelectStatus(status) }
        }
    }

    fun selectFeed(feed: Feed, folderTitle: String?) {
        coroutineScope.launch {
            if (!filter.isFeedSelected(feed)) {
                openNextList { onSelectFeed(feed.id, folderTitle) }
            } else {
                closeDrawer()
            }
        }
    }

    fun selectFolder(folder: Folder) {
        if (!filter.isFolderSelect(folder)) {
            openNextList { onSelectFolder(folder.title) }
        } else {
            closeDrawer()
        }
    }

    fun selectSavedSearch(savedSearch: SavedSearch) {
        if (!filter.isSavedSearchSelected(savedSearch)) {
            openNextList { onSelectSavedSearch(savedSearch.id) }
        } else {
            closeDrawer()
        }
    }

    ArticleHandler {
        selectArticle(it)
    }

    ArticleScaffold(
        drawerState = drawerState,
        scaffoldNavigator = scaffoldNavigator,
        drawerPane = {
            FeedList(
                folders = folders,
                feeds = feeds,
                onSelectFolder = ::selectFolder,
                onSelectFeed = ::selectFeed,
                onFeedAdded = { onFeedAdded(it) },
                savedSearches = savedSearches,
                onSelectSavedSearch = ::selectSavedSearch,
                onNavigateToSettings = onNavigateToSettings,
                onFilterSelect = ::selectFilter,
                filter = filter,
                statusCount = statusCount,
                onSelectStatus = ::selectStatus
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
                    FeedListTopBar(
                        onRequestJumpToTop = {
                            scrollToTop()
                        },
                        onNavigateToDrawer = {
                            openDrawer()
                        },
                        onRequestSnackbar = { showSnackbar(it) },
                        onRemoveFeed = onRemoveFeed,
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
                    SnackbarHost(hostState = snackbarHost)
                },
            ) { innerPadding ->
                ArticleListScaffold(
                    padding = innerPadding,
                    showOnboarding = isInitialized && !isRefreshing && allFeeds.isEmpty(),
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
                            Crossfade(
                                articles,
                                animationSpec = tween(500),
                                modifier = Modifier.fillMaxSize(),
                                label = "",
                            ) {
                                ArticleList(
                                    articles = it,
                                    selectedArticleKey = article?.id,
                                    listState = listState,
                                    enableMarkReadOnScroll = enableMarkReadOnScroll,
                                    onMarkAllRead = { range ->
                                        onMarkAllRead(range)
                                    },
                                    onSelect = ::selectArticle,
                                )
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            if (article == null && hasMultipleColumns) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CapyPlaceholder()
                }
            } else if (article != null) {
                val indexedArticles =
                    rememberIndexedArticles(article = article, articles = articles)

                ArticleView(
                    article = article,
                    onNavigateToMedia = { media = it },
                    articles = indexedArticles,
                    onBackPressed = {
                        clearArticle()
                    },
                    onToggleRead = onToggleArticleRead,
                    onToggleStar = onToggleArticleStar,
                    enableBackHandler = media == null,
                    onRequestArticle = { id ->
                        coroutineScope.launchUI {
                            onSelectArticle(id)
                        }
                    },
                )

                LaunchedEffect(article.id, indexedArticles.index) {
                    if (indexedArticles.canScroll) {
                        CapyLog.info(
                            "list_scroll",
                            mapOf("page" to indexedArticles.index.toString())
                        )
                        scrollToArticle(indexedArticles.index)
                    }
                }
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
            initialize()
        }
    }

    BackHandler(media != null) {
        media = null
    }

    BackHandler(media == null && search.isActive && article == null) {
        search.clear()
    }

    ArticleListBackHandler(
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

fun findCurrentFeed(filter: ArticleFilter, feeds: List<Feed>): Feed? {
    if (filter is ArticleFilter.Feeds) {
        return feeds.find { it.id == filter.feedID }
    }

    return null
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
