@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomAppBarDefaults.exitAlwaysScrollBehavior
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.Media
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.Article
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Shared animation spec for synchronized toolbar animations (matching ReadYou's implementation)
private val toolbarAnimationSpec = spring<IntSize>(stiffness = 700f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleView(
    article: Article,
    articles: LazyPagingItems<Article>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onScrollToArticle: (index: Int) -> Unit,
    onSelectArticle: (id: String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    appPreferences: AppPreferences = koinInject()
) {
    val fullContent = LocalFullContent.current
    val openLink = articleOpenLink(article)

    val onToggleFullContent = {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    val index = remember(
        article.id,
        articles.itemCount,
    ) {
        articles.itemSnapshotList.indexOfFirst { it?.id == article.id }
    }

    val previousIndex = index - 1
    val nextIndex = index + 1

    val enableHorizontalPager by appPreferences.readerOptions.enableHorizontaPagination.collectChangesWithDefault()
    val topToolbarPreference = rememberTopToolbarPreference(key = article.id)
    val bottomScrollBehavior = key(article.id) { exitAlwaysScrollBehavior() }
    val enableBottomBar by rememberBottomBarPreference()
    val enableSwipeNavigation by appPreferences.readerOptions.enableSwipeNavigation.collectChangesWithDefault()

    val hasPrevious = previousIndex > -1 && articles[index - 1] != null && enableSwipeNavigation
    val hasNext = nextIndex < articles.itemCount && articles[index + 1] != null && enableSwipeNavigation

    fun selectPrevious() {
        articles[previousIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    fun selectNext() {
        articles[nextIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    val previousArticle = if (hasPrevious) articles[previousIndex] else null
    val nextArticle = if (hasNext) articles[nextIndex] else null
    val transitionState = ArticleTransitionState(
        articleId = article.id,
        previousArticleId = previousArticle?.id,
        nextArticleId = nextArticle?.id,
    )

    ArticleViewScaffold(
        bottomScrollBehavior = bottomScrollBehavior,
        enableBottomBar = enableBottomBar,
        topToolbarPreference = topToolbarPreference,
        topBar = {
            ArticleTopBar(
                scrollBehavior = topToolbarPreference.scrollBehavior,
                onClose = onBackPressed,
                actions = {
                    if (!enableBottomBar) {
                        ArticleActions(
                            article = article,
                            onToggleExtractContent = onToggleFullContent,
                            onToggleRead = onToggleRead,
                            onToggleStar = onToggleStar,
                        )
                    }
                }
            )
        },
        bottomBar = {
            FlexibleBottomAppBar(
                expandedHeight = 56.dp,
                scrollBehavior = bottomScrollBehavior,
            ) {
                ArticleActions(
                    article = article,
                    onToggleExtractContent = onToggleFullContent,
                    onToggleRead = onToggleRead,
                    onToggleStar = onToggleStar,
                )
            }
        },
        reader = {
            ArticleTransition(
                article = transitionState
            ) {
                val pullDownAction: (() -> Unit)? = if (hasPrevious) {
                    { selectPrevious() }
                } else null

                val pullUpAction: (() -> Unit)? = if (hasNext) {
                    { selectNext() }
                } else null

                val pullToLoadState = rememberPullToLoadState(
                    key = article.id,
                    onLoadNext = pullUpAction,
                    onLoadPrevious = pullDownAction,
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalReaderPager(
                        enabled = enableHorizontalPager,
                        enablePrevious = hasPrevious,
                        enableNext = hasNext,
                        onSelectPrevious = {
                            selectPrevious()
                        },
                        onSelectNext = {
                            selectNext()
                        },
                    ) {
                        key(article.id) {
                            ArticleReader(
                                modifier = Modifier.pullToLoad(
                                    state = pullToLoadState,
                                    enabled = true
                                ),
                                article = article,
                                onSelectMedia = onSelectMedia,
                            )
                        }
                    }

                    PullToLoadIndicator(
                        state = pullToLoadState,
                        canLoadNext = hasNext,
                        canLoadPrevious = hasPrevious,
                    )
                }
            }
        },
    )

    LaunchedEffect(index) {
        if (index > -1) {
            onScrollToArticle(index)
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
private fun ArticleViewScaffold(
    topBar: @Composable () -> Unit,
    enableBottomBar: Boolean,
    bottomBar: @Composable () -> Unit,
    reader: @Composable () -> Unit,
    bottomScrollBehavior: BottomAppBarScrollBehavior,
    topToolbarPreference: ToolbarPreferences,
) {
    val showToolbars = topToolbarPreference.show

    Scaffold(
        modifier = Modifier
            .nestedScroll(bottomScrollBehavior.nestedScrollConnection)
            .nestedScroll(topToolbarPreference.scrollBehavior.nestedScrollConnection),
        topBar = {
            if (topToolbarPreference.pinned) {
                topBar()
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column {
                    reader()
                }
            }

            if (!topToolbarPreference.pinned) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    AnimatedVisibility(
                        visible = showToolbars,
                        enter = expandVertically(
                            expandFrom = Alignment.Bottom,
                            animationSpec = toolbarAnimationSpec
                        ),
                        exit = shrinkVertically(
                            shrinkTowards = Alignment.Bottom,
                            animationSpec = toolbarAnimationSpec
                        )
                    ) {
                        topBar()
                    }
                }
            }

            if (enableBottomBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .zIndex(1f)
                ) {
                    AnimatedVisibility(
                        visible = showToolbars,
                        enter = expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = toolbarAnimationSpec
                        ),
                        exit = shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = toolbarAnimationSpec
                        )
                    ) {
                        bottomBar()
                    }
                }
            }
        }
    }
}

@Composable
fun rememberBottomBarPreference(appPreferences: AppPreferences = koinInject()): State<Boolean> {
    return appPreferences.readerOptions.bottomBarActions
        .stateIn(rememberCoroutineScope())
        .collectAsState()
}

@Composable
fun rememberTopToolbarPreference(
    key: Any? = null,
    appPreferences: AppPreferences = koinInject(),
): ToolbarPreferences {
    val pinTopToolbar by appPreferences.readerOptions.pinTopToolbar.stateIn(rememberCoroutineScope())
        .collectAsState()

    val scrollBehavior = key(key) {
        if (pinTopToolbar) {
            TopAppBarDefaults.pinnedScrollBehavior()
        } else {
            TopAppBarDefaults.enterAlwaysScrollBehavior()
        }
    }

    val showToolbars = scrollBehavior.state.collapsedFraction == 0f

    return ToolbarPreferences(scrollBehavior, showToolbars, pinTopToolbar)
}

@Stable
data class ToolbarPreferences(
    val scrollBehavior: TopAppBarScrollBehavior,
    val show: Boolean,
    val pinned: Boolean,
)

@Composable
fun articleOpenLink(
    article: Article,
): () -> Unit {
    val linkOpener = LocalLinkOpener.current

    fun open() {
        val link = article.url?.toString() ?: return

        linkOpener.open(link.toUri())
    }

    return ::open
}
