@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.Media
import com.capyreader.app.ui.articles.IndexedArticles
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.capyreader.app.ui.settings.panels.ArticleVerticalSwipe
import com.capyreader.app.ui.settings.panels.ArticleVerticalSwipe.LOAD_FULL_CONTENT
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    onNavigateToMedia: (media: Media) -> Unit,
    articles: IndexedArticles,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (id: String) -> Unit
) {
    val fullContent = LocalFullContent.current
    val startPage = rememberSaveable { articles.index }

    fun selectArticle(relation: () -> Article?) {
        relation()?.let { onRequestArticle(it.id) }
    }

    val onRequestPrevious = {
        selectArticle { articles.previous() }
    }

    val onRequestNext = {
        selectArticle { articles.next() }
    }

    val onToggleFullContent = {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    val toolbars = rememberToolbarPreferences(articleID = article.id)

    val pagerState = rememberPagerState(
        initialPage = articles.index,
        pageCount = {
            articles.size
        }
    )

    ArticleViewScaffold(
        topBar = {
            ArticleTopBar(
                article = article,
                scrollBehavior = toolbars.scrollBehavior,
                onToggleExtractContent = onToggleFullContent,
                onToggleRead = onToggleRead,
                onToggleStar = onToggleStar,
                onClose = onBackPressed
            )
        },
        reader = {
            HorizontalPager(state = pagerState) { page ->
                ArticlePullRefresh(
                    toolbars.show && !toolbars.pinned,
                    onToggleFullContent = onToggleFullContent,
                    onRequestNext = onRequestNext,
                    onRequestPrevious = onRequestPrevious,
                    articles = articles,
                ) {
                    CapyLog.info("reader", mapOf("page" to page.toString()))
                    articles.find(page)?.let { pagedArticle ->
                        ArticleReader(
                            article = currentArticle(article, pagedArticle),
                            onNavigateToMedia = onNavigateToMedia,
                        )
                    }
                }
            }
        },
        bottomBar = {
            ArticleBottomBar(
                onRequestNext = onRequestNext,
                showNext = articles.hasNext()
            )
        },
        toolbarPreferences = toolbars
    )

    LaunchedEffect(pagerState.currentPage) {
        val currentArticle = articles.find(pagerState.currentPage) ?: return@LaunchedEffect

        if (currentArticle.id != article.id) {
            onRequestArticle(currentArticle.id)
        }
    }

    LaunchedEffect(articles.index) {
        pagerState.scrollToPage(articles.index)
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
private fun ArticleViewScaffold(
    topBar: @Composable () -> Unit,
    reader: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    toolbarPreferences: ToolbarPreferences,
) {
    Scaffold(
        modifier = Modifier.nestedScroll(toolbarPreferences.scrollBehavior.nestedScrollConnection),
        topBar = {
            if (toolbarPreferences.pinned) {
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
                    Column(Modifier.weight(0.1f)) {
                        reader()
                    }

                    if (toolbarPreferences.pinned) {
                        bottomBar()
                    }
                }

                if (!toolbarPreferences.pinned) {
                    BarVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = toolbarPreferences.show,
                    ) {
                        bottomBar()
                    }
                }
            }

            if (!toolbarPreferences.pinned) {
                topBar()
            }
        }
    }
}

@Composable
fun ArticlePullRefresh(
    includePadding: Boolean,
    onToggleFullContent: () -> Unit,
    onRequestNext: () -> Unit,
    onRequestPrevious: () -> Unit,
    articles: IndexedArticles,
    content: @Composable () -> Unit,
) {
    val (topSwipe, bottomSwipe) = rememberSwipePreferences()
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val onPullUp = {
        if (topSwipe == LOAD_FULL_CONTENT) {
            onToggleFullContent()
        } else {
            onRequestPrevious()
        }
    }

    val enableTopSwipe = topSwipe.enabled &&
            (topSwipe == LOAD_FULL_CONTENT || (topSwipe.openArticle && articles.hasPrevious()))

    SwipeRefresh(
        onRefresh = { onPullUp() },
        swipeEnabled = enableTopSwipe,
        icon = if (topSwipe == LOAD_FULL_CONTENT) {
            Icons.AutoMirrored.Rounded.Article
        } else {
            Icons.Rounded.KeyboardArrowUp
        },
        indicatorPadding = PaddingValues(
            top = if (includePadding) {
                TopBarOffset
            } else {
                0.dp
            }
        ),
        onTriggerThreshold = { triggerThreshold() }
    ) {
        SwipeRefresh(
            onRefresh = { onRequestNext() },
            swipeEnabled = bottomSwipe.enabled && articles.hasNext(),
            onTriggerThreshold = { triggerThreshold() },
            indicatorPadding = PaddingValues(
                bottom = if (includePadding) {
                    BottomBarOffset
                } else {
                    0.dp
                }
            ),
            indicatorAlignment = Alignment.BottomCenter,
        ) {
            content()
        }
    }
}

private val TopBarOffset = 56.dp

private val BottomBarOffset = 44.dp

@Composable
fun BarVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun rememberToolbarPreferences(
    articleID: String,
    appPreferences: AppPreferences = koinInject(),
): ToolbarPreferences {
    val topBarState = rememberSaveable(articleID, saver = TopAppBarState.Saver) {
        TopAppBarState(0f, 0f, 0f)
    }

    val pinToolbars = appPreferences.readerOptions.pinToolbars
        .stateIn(rememberCoroutineScope())
        .collectAsState()
        .value

    val scrollBehavior = if (pinToolbars) {
        TopAppBarDefaults.pinnedScrollBehavior(state = topBarState)
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior(state = topBarState)
    }

    val showToolbars = scrollBehavior.state.collapsedFraction == 0f

    return ToolbarPreferences(scrollBehavior, showToolbars, pinToolbars)
}

@Stable
data class ToolbarPreferences(
    val scrollBehavior: TopAppBarScrollBehavior,
    val show: Boolean,
    val pinned: Boolean,
)

@Composable
private fun rememberSwipePreferences(appPreferences: AppPreferences = koinInject()): SwipePreferences {
    val coroutineScope = rememberCoroutineScope()
    val topSwipe by appPreferences.readerOptions.topSwipeGesture
        .stateIn(coroutineScope)
        .collectAsState()

    val bottomSwipe by appPreferences.readerOptions.bottomSwipeGesture
        .stateIn(coroutineScope)
        .collectAsState()

    return SwipePreferences(topSwipe, bottomSwipe)
}

@Stable
private data class SwipePreferences(
    val topSwipe: ArticleVerticalSwipe,
    val bottomSwipe: ArticleVerticalSwipe,
)

fun currentArticle(article: Article, pagedArticle: Article): Article {
    return if (article.id == pagedArticle.id) {
        article
    } else {
        pagedArticle
    }
}
