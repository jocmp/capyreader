package com.jocmp.basilreader.ui.articles

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
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
    ExperimentalMaterialApi::class,
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

    val (refreshing, setRefreshing) = remember { mutableStateOf(false) }

    val refresh = {
        setRefreshing(true)
        onFeedRefresh {
            setRefreshing(false)
        }
    }

    val state = rememberPullRefreshState(refreshing, refresh)

    ArticleScaffold(
        drawerState = drawerState,
        listDetailState = scaffoldState,
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
                        .pullRefresh(state)
                ) {
                    Crossfade(
                        articles,
                        label = ""
                    ) {
                        ArticleList(
                            articles = it,
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

                    PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
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
                    setDestination(ListDetailPaneScaffoldRole.List)
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
            onToggleArticleStar = {}
        )
    }
}
