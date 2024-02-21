package com.jocmp.basilreader.ui.articles

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.material3.adaptive.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.components.rememberSaveableWebViewState
import com.jocmp.basilreader.ui.components.rememberWebViewNavigator
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture
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
    articles: Flow<PagingData<Article>>,
    article: Article?,
    statusCount: Long,
    onAddFeed: () -> Unit,
    onFeedRefresh: (completion: () -> Unit) -> Unit,
    onSelectFolder: (folderTitle: String) -> Unit,
    onSelectFeed: (feedID: String) -> Unit,
    onSelectArticleFilter: () -> Unit,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    onSelectArticle: (articleID: String, completion: (article: Article) -> Unit) -> Unit,
    onEditFolder: (folderTitle: String) -> Unit,
    onEditFeed: (feedID: String) -> Unit,
    onRemoveFeed: (feedID: String) -> Unit,
    onRemoveFolder: (folderTitle: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onClearArticle: () -> Unit,
    onToggleArticleRead: () -> Unit,
    onToggleArticleStar: () -> Unit,
    drawerValue: DrawerValue = DrawerValue.Closed
) {
    val filterStatus = filter.status
    val drawerState = rememberDrawerState(drawerValue)
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<ListDetailPaneScaffoldRole>(
        scaffoldDirective = calculateArticleDirective()
    )

    val context = LocalContext.current
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberSaveableWebViewState()

    val navigateToDetail = {
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    val onComplete = {
        coroutineScope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
            delay(200)
            drawerState.close()
        }
    }

    val state = rememberPullToRefreshState()

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onFeedRefresh {
                state.endRefresh()
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
                onAddFeed = onAddFeed,
                onSelectFolder = {
                    onSelectFolder(it)
                    onComplete()
                },
                onSelectFeed = {
                    onSelectFeed(it)
                    onComplete()
                },
                onNavigateToAccounts = onNavigateToAccounts,
                onFilterSelect = {
                    onSelectArticleFilter()
                    onComplete()
                },
                filter = filter,
                statusCount = statusCount
            )
        },
        listPane = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { FilterAppBarTitle(filter) },
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
                                onFeedEdit = onEditFeed,
                                onFolderEdit = onEditFolder,
                                onRemoveFeed = onRemoveFeed,
                                onRemoveFolder = onRemoveFolder,
                            )
                        }
                    )
                },
                bottomBar = {
                    ArticleFilterNavigationBar(
                        selected = filterStatus,
                        onSelect = onSelectStatus,
                    )
                }
            ) { innerPadding ->
                Box(
                    Modifier
                        .padding(innerPadding)
                        .nestedScroll(state.nestedScrollConnection)
                ) {
                    Crossfade(
                        articles,
                        label = ""
                    ) {
                        ArticleList(
                            articles = it,
                            selectedArticleKey = article?.key,
                            onSelect = { articleID ->
                                onSelectArticle(articleID) {
                                    coroutineScope.launch {
                                        val html = ArticleRenderer.render(it, context)
                                        webViewNavigator.loadHtml(html)
                                        navigateToDetail()
                                    }
                                }
                            }
                        )
                    }

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
                    navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                }
            )
        }
    )

    LaunchedEffect(webViewNavigator) {
        val html = ArticleRenderer.render(article, context)

        if (webViewState.viewState == null) {
            webViewNavigator.loadHtml(html)
        }
    }
}

@Preview
@Composable
fun ArticleLayoutPreview() {
    val folders = FolderPreviewFixture().values.take(2).toList()
    val feeds = FeedPreviewFixture().values.take(2).toList()

    MaterialTheme {
        ArticleLayout(
            filter = ArticleFilter.default(),
            folders = folders,
            feeds = feeds,
            articles = emptyFlow(),
            article = null,
            statusCount = 30,
            drawerValue = DrawerValue.Open,
            onAddFeed = { },
            onFeedRefresh = {},
            onSelectFolder = {},
            onSelectFeed = {},
            onSelectArticleFilter = { },
            onSelectStatus = {},
            onSelectArticle = { _, _ -> },
            onEditFolder = {},
            onEditFeed = {},
            onRemoveFeed = {},
            onRemoveFolder = {},
            onNavigateToAccounts = { },
            onClearArticle = { },
            onToggleArticleRead = { },
            onToggleArticleStar = {},
        )
    }
}
