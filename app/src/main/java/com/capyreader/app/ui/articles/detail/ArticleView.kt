@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.Media
import com.capyreader.app.common.openLink
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.capyreader.app.ui.settings.panels.ArticleVerticalSwipe
import com.capyreader.app.ui.settings.panels.ArticleVerticalSwipe.LOAD_FULL_CONTENT
import com.capyreader.app.ui.settings.panels.ArticleVerticalSwipe.OPEN_ARTICLE_IN_BROWSER
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    pagerState: PagerState,
    pagerInitialized: Boolean,
    onNavigateToMedia: (media: Media) -> Unit,
    articles: LazyPagingItems<Article>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onRequestArticle: (index: Int, articleID: String) -> Unit,
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

    val onSwipe = { swipe: ArticleVerticalSwipe ->
        when (swipe) {
            LOAD_FULL_CONTENT -> onToggleFullContent()
            OPEN_ARTICLE_IN_BROWSER -> openLink()
            else -> {}
        }
    }

    val toolbars = rememberToolbarPreferences(articleID = article.id)

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
            ArticlePullRefresh(
                toolbars.show && !toolbars.pinned,
                onSwipe = onSwipe,
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) { page ->
                    articles[page]?.let { pagedArticle ->
                        ArticleReader(
                            article = currentArticle(article, pagedArticle),
                            onNavigateToMedia = onNavigateToMedia,
                        )
                    }
                }
            }
        },
        toolbarPreferences = toolbars
    )

    LaunchedEffect(pagerInitialized, pagerState.currentPage) {
        if (!pagerInitialized) {
            return@LaunchedEffect
        }

        val currentArticle = articles[pagerState.currentPage]

        if (currentArticle != null) {
            CapyLog.info("scroll_to", mapOf("page" to pagerState.currentPage.toString(), "article_id" to currentArticle.id))
            onRequestArticle(pagerState.currentPage, currentArticle.id)
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}

@Composable
private fun ArticleViewScaffold(
    topBar: @Composable () -> Unit,
    reader: @Composable () -> Unit,
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
                    reader()
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
    onSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    content: @Composable () -> Unit,
) {
    val (topSwipe, bottomSwipe) = rememberSwipePreferences()
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val enableTopSwipe = topSwipe.enabled

    val enableBottomSwipe = bottomSwipe.enabled

    SwipeRefresh(
        onRefresh = { onSwipe(topSwipe) },
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
            onRefresh = { onSwipe(bottomSwipe) },
            swipeEnabled = enableBottomSwipe,
            icon = if (bottomSwipe == OPEN_ARTICLE_IN_BROWSER) {
                Icons.AutoMirrored.Rounded.OpenInNew
            } else {
                Icons.Rounded.KeyboardArrowDown
            },
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

@Composable
fun articleOpenLink(
    article: Article,
    appPreferences: AppPreferences = koinInject()
): () -> Unit {
    val context = LocalContext.current

    fun open() {
        val link = article.url?.toString() ?: return

        context.openLink(Uri.parse(link), appPreferences)
    }

    return ::open
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
        pagedArticle.withPlaceholderContent()
    }
}

private fun Article.withPlaceholderContent(): Article {
    return if (enableStickyFullContent) {
        copy(summary = "", content = "")
    } else {
        this
    }
}
