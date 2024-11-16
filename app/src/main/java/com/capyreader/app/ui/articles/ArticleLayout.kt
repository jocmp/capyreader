package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.common.Media
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.detail.resetScrollBehaviorListener
import com.capyreader.app.ui.articles.list.EmptyOnboardingView
import com.capyreader.app.ui.articles.list.FeedListTopBar
import com.capyreader.app.ui.articles.media.ArticleMediaView
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.rememberWebViewState
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.capyreader.app.ui.isCompact
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.common.launchUI
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
    allFolders: List<Folder>,
    articles: Flow<PagingData<Article>>,
    article: Article?,
    search: ArticleSearch,
    statusCount: Long,
    refreshInterval: RefreshInterval,
    onFeedRefresh: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: suspend (feedID: String) -> Unit,
    onSelectArticleFilter: () -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onSelectArticle: (articleID: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onRequestClearArticle: () -> Unit,
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
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val pagingArticles = articles.collectAsLazyPagingItems(Dispatchers.IO)
    val snackbarHost = remember { SnackbarHostState() }
    val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
    val currentFeed = findCurrentFeed(filter, allFeeds)
    val scrollBehavior = pinnedScrollBehavior()
    val resetScrollBehaviorOffset = resetScrollBehaviorListener(
        listState = listState,
        scrollBehavior = scrollBehavior
    )
    var media by rememberSaveable { mutableStateOf<Media?>(null) }
    val focusManager = LocalFocusManager.current
    val openUpdatePasswordDialog = {
        onUnauthorizedDismissRequest()
        setUpdatePasswordDialogOpen(true)
    }

    suspend fun navigateToDetail() {
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    val webViewState = rememberWebViewState(
        onNavigateToMedia = {
            media = it
        },
    )

    fun scrollToArticle(index: Int) {
        coroutineScope.launch {
            if (index > -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    val scrollToTop = {
        coroutineScope.launch {
            listState.scrollToItem(0)
        }
    }

    val resetScrollOffset = {
        coroutineScope.launch {
            pagingArticles.refresh()
            delay(500)
            resetScrollBehaviorOffset()
        }
    }

    val refreshFeeds = {
        isRefreshing = true
        onFeedRefresh {
            isRefreshing = false
            resetScrollOffset()

            if (!isInitialized) {
                setInitialized(true)
            }
        }
    }

    val openNextList = suspend {
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
        delay(200)
        drawerState.close()
        resetScrollOffset()
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

    val closeDrawer = {
        coroutineScope.launch {
            drawerState.close()
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
            onSelectFeed(feedID)
            openNextList()

            showSnackbar(addFeedSuccessMessage)
        }
    }

    val selectArticle = { articleID: String ->
        onSelectArticle(articleID)
        if (search.isActive) {
            focusManager.clearFocus()
        }
        coroutineScope.launch {
            navigateToDetail()
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
                onSelectFolder = {
                    if (!filter.isFolderSelect(it)) {
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
                            onSelectFeed(it.id)
                            openNextList()
                        } else {
                            closeDrawer()
                        }
                    }
                },
                onFeedAdded = { onFeedAdded(it) },
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
                onSelectStatus = {
                    onSelectStatus(it)
                    resetScrollOffset()
                }
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
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        onRequestSnackbar = { showSnackbar(it) },
                        onRemoveFeed = onRemoveFeed,
                        onSearchQueryChange = {
                            scrollToTop()
                        },
                        scrollBehavior = scrollBehavior,
                        onMarkAllRead = onMarkAllRead,
                        search = search,
                        filter = filter,
                        currentFeed = currentFeed,
                        feeds = allFeeds,
                        allFolders = allFolders
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHost)
                },
            ) { innerPadding ->
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = refreshFeeds,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    if (isInitialized && !isRefreshing && allFeeds.isEmpty()) {
                        EmptyOnboardingView {
                            AddFeedButton(
                                onComplete = {
                                    onFeedAdded(it)
                                }
                            )
                        }
                    } else {
                        ArticleList(
                            articles = pagingArticles,
                            selectedArticleKey = article?.id,
                            listState = listState,
                            onMarkAllRead = onMarkAllRead,
                            onSelect = { selectArticle(it) },
                        )
                    }
                }
            }
        },
        detailPane = {
            if (article == null && !isCompact()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CapyPlaceholder()
                }
            } else if (article != null) {
                val compact = isCompact()
                val indexedArticles =
                    rememberIndexedArticles(article = article, articles = pagingArticles)

                ArticleView(
                    article = article,
                    webViewState = webViewState,
                    articles = indexedArticles,
                    onBackPressed = {
                        coroutineScope.launchUI {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        }
                        onRequestClearArticle()
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
                    if (!compact) {
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
            refreshFeeds()
        }
    }

    BackHandler(media != null) {
        media = null
    }

    BackHandler(media == null && search.isActive && article == null) {
        search.clear()
    }

    ArticleListBackHandler(
        enabled = isFeedActive(media, article, search)
    ) {
        toggleDrawer()
    }
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

@Preview
@Composable
fun ArticleLayoutPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedSample().values.take(2).toList()

    MaterialTheme {
        ArticleLayout(
            filter = ArticleFilter.default(),
            folders = folders,
            feeds = feeds,
            allFolders = emptyList(),
            allFeeds = emptyList(),
            articles = emptyFlow(),
            search = ArticleSearch(),
            article = null,
            statusCount = 30,
            refreshInterval = RefreshInterval.MANUALLY_ONLY,
            onFeedRefresh = {},
            onSelectFolder = {},
            onSelectFeed = {},
            onSelectArticleFilter = { },
            onSelectStatus = {},
            onSelectArticle = {},
            onNavigateToSettings = { },
            onRequestClearArticle = { },
            onToggleArticleRead = { },
            onToggleArticleStar = { },
            onMarkAllRead = { },
            onRemoveFeed = { _, _, _ -> },
            drawerValue = DrawerValue.Open,
            showUnauthorizedMessage = false,
            onUnauthorizedDismissRequest = {},
        )
    }
}
