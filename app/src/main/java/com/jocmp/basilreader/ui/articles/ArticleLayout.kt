package com.jocmp.basilreader.ui.articles

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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.rememberListDetailPaneScaffoldNavigator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.articles.detail.ArticleRenderer
import com.jocmp.basilreader.ui.articles.detail.ArticleView
import com.jocmp.basilreader.ui.articles.detail.articleTemplateColors
import com.jocmp.basilreader.ui.components.rememberSaveableWebViewState
import com.jocmp.basilreader.ui.components.rememberWebViewNavigator
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture
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
    onFeedRefresh: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: suspend (feedID: String) -> Unit,
    onSelectArticleFilter: () -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onSelectArticle: (articleID: String, completion: (article: Article) -> Unit) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onClearArticle: () -> Unit,
    onToggleArticleRead: () -> Unit,
    onToggleArticleStar: () -> Unit,
    onRemoveFeed: (feedID: String, onSuccess: () -> Unit, onFailure: () -> Unit) -> Unit,
    drawerValue: DrawerValue = DrawerValue.Closed,
) {
    val articleColors = articleTemplateColors()
    val (isInitialized, setInitialized) = rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(drawerValue)
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<ListDetailPaneScaffoldRole>(
        scaffoldDirective = calculateArticleDirective()
    )

    val context = LocalContext.current
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberSaveableWebViewState()
    val listState = rememberLazyListState()
    val pagingArticles = articles.collectAsLazyPagingItems(Dispatchers.IO)
    val state = rememberPullToRefreshState()
    val snackbarHost = remember { SnackbarHostState() }
    val addFeedSuccessMessage = stringResource(R.string.add_feed_success)
    val editSuccessMessage = stringResource(R.string.feed_action_edit_success)
    val unsubscribeMessage = stringResource(R.string.feed_action_unsubscribe_success)
    val unsubscribeErrorMessage = stringResource(R.string.unsubscribe_error)
    val currentFeed = findCurrentFeed(filter, allFeeds)

    val navigateToDetail = {
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    val navigateToList = suspend {
        navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        listState.scrollToItem(0)
        delay(200)
        drawerState.close()
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
                coroutineScope.launch {
                    delay(200)
                    listState.scrollToItem(0)
                }
            }
        }
    }

    ArticleScaffold(
        drawerState = drawerState,
        listDetailState = navigator.scaffoldState,
        drawerPane = {
            FeedList(
                folders = folders,
                feeds = feeds,
                onSelectFolder = {
                    onSelectFolder(it)
                    coroutineScope.launch {
                        navigateToList()
                    }
                },
                onSelectFeed = {
                    coroutineScope.launch {
                        onSelectFeed(it)
                        navigateToList()
                    }
                },
                onFeedAdded = { feedID ->
                    coroutineScope.launch {
                        onSelectFeed(feedID)
                        navigateToList()

                        showSnackbar(addFeedSuccessMessage)

                        launch {
                            state.startRefresh()
                        }
                    }
                },
                onNavigateToAccounts = onNavigateToAccounts,
                onFilterSelect = {
                    onSelectArticleFilter()
                    coroutineScope.launch {
                        navigateToList()
                    }
                },
                filter = filter,
                statusCount = statusCount,
                onStatusSelect = onSelectStatus,
            )
        },
        listPane = {
            Scaffold(
                topBar = {
                    TopAppBar(
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
                            if (currentFeed != null) {
                                FilterActionMenu(
                                    feed = currentFeed,
                                    folders = folders,
                                    onFeedEdited = {
                                        showSnackbar(editSuccessMessage)
                                    },
                                    onRequestRemoveFeed = { feedID ->
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
                        onSelect = { articleID ->
                            onSelectArticle(articleID) {
                                coroutineScope.launch {
                                    val html =
                                        ArticleRenderer.render(
                                            it,
                                            colors = articleColors,
                                            context = context
                                        )
                                    webViewNavigator.loadHtml(html)
                                    navigateToDetail()
                                }
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
                webViewState = webViewState,
                webViewNavigator = webViewNavigator,
                onBackPressed = {
                    onClearArticle()
                    navigator.navigateBack()
                }
            )
        }
    )

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            state.startRefresh()

            setInitialized(true)
        }
    }

    LaunchedEffect(webViewNavigator) {
        val html = ArticleRenderer.render(article, articleColors, context)

        if (webViewState.viewState == null) {
            webViewNavigator.loadHtml(html)
        }
    }

    LaunchedEffect(articleColors) {
        val html = ArticleRenderer.render(article, articleColors, context)

        webViewNavigator.loadHtml(html)
    }
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
    val feeds = FeedPreviewFixture().values.take(2).toList()

    MaterialTheme {
        ArticleLayout(
            filter = ArticleFilter.default(),
            allFeeds = emptyList(),
            folders = folders,
            feeds = feeds,
            articles = emptyFlow(),
            article = null,
            statusCount = 30,
            onFeedRefresh = {},
            onSelectFolder = {},
            onSelectFeed = {},
            onSelectArticleFilter = { },
            onSelectStatus = {},
            onSelectArticle = { _, _ -> },
            onRemoveFeed = { _, _, _ -> },
            onNavigateToAccounts = { },
            onClearArticle = { },
            onToggleArticleRead = { },
            onToggleArticleStar = {},
            drawerValue = DrawerValue.Open,
        )
    }
}
