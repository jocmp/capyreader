@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomAppBarDefaults.exitAlwaysScrollBehavior
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.jocmp.capy.common.launchUI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.Media
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe.DISABLED
import com.capyreader.app.preferences.ArticleVerticalSwipe.LOAD_FULL_CONTENT
import com.capyreader.app.preferences.ArticleVerticalSwipe.NEXT_ARTICLE
import com.capyreader.app.preferences.ArticleVerticalSwipe.OPEN_ARTICLE_IN_BROWSER
import com.capyreader.app.preferences.ArticleVerticalSwipe.PREVIOUS_ARTICLE
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleView(
    article: Article,
    articles: LazyPagingItems<Article>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    isDetailPaneFocused: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocusList: () -> Unit = {},
    onFocusDetail: () -> Unit = {},
    onScrollToArticle: (index: Int) -> Unit,
    onSelectArticle: (id: String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    appPreferences: AppPreferences = koinInject()
) {
    val enableHorizontalPager by appPreferences.readerOptions.enableHorizontaPagination.collectChangesWithDefault()
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

    val hasPrevious = previousIndex > -1 && articles[index - 1] != null
    val hasNext = nextIndex < articles.itemCount && articles[index + 1] != null

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

    val onSwipe = { swipe: ArticleVerticalSwipe ->
        when (swipe) {
            LOAD_FULL_CONTENT -> onToggleFullContent()
            OPEN_ARTICLE_IN_BROWSER -> openLink()
            PREVIOUS_ARTICLE -> selectPrevious()
            NEXT_ARTICLE -> selectNext()
            DISABLED -> {}
        }
    }

    var readerScrollState by remember { mutableStateOf<ScrollState?>(null) }
    var readerMaxHeight by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    val onScrollStateReady = { scrollState: ScrollState, maxHeight: Float ->
        readerScrollState = scrollState
        readerMaxHeight = maxHeight
    }

    val scrollReader = { direction: Int ->
        readerScrollState?.let { scrollState ->
            val scrollAmount = readerMaxHeight * SCROLL_PROPORTION * direction
            coroutineScope.launchUI {
                scrollState.scrollBy(scrollAmount)
            }
        }
        Unit
    }

    val onShortcut = { shortcut: ArticleShortcut ->
        when (shortcut) {
            ArticleShortcut.NextArticle -> selectNext()
            ArticleShortcut.PreviousArticle -> selectPrevious()
            ArticleShortcut.OpenInBrowser -> openLink()
            ArticleShortcut.ToggleStar -> onToggleStar()
            ArticleShortcut.ToggleRead -> onToggleRead()
            ArticleShortcut.ToggleFullContent -> onToggleFullContent()
            ArticleShortcut.GoBack -> onBackPressed()
            ArticleShortcut.ScrollDown -> scrollReader(1)
            ArticleShortcut.ScrollUp -> scrollReader(-1)
            ArticleShortcut.FocusList -> onFocusList()
            ArticleShortcut.FocusDetail -> onFocusDetail()
        }
    }

    val topToolbarPreference = rememberTopToolbarPreference()
    val bottomScrollBehavior = exitAlwaysScrollBehavior()
    val enableBottomBar by rememberBottomBarPreference()

    LaunchedEffect(article.id) {
        topToolbarPreference.scrollBehavior.state.heightOffset = 0f
        topToolbarPreference.scrollBehavior.state.contentOffset = 0f
        bottomScrollBehavior.state.heightOffset = 0f
        bottomScrollBehavior.state.contentOffset = 0f
    }

    ArticleViewScaffold(
        bottomScrollBehavior = bottomScrollBehavior,
        enableBottomBar = enableBottomBar,
        topToolbarPreference = topToolbarPreference,
        focusRequester = focusRequester,
        isDetailPaneFocused = isDetailPaneFocused,
        onShortcut = onShortcut,
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
            ArticlePullRefresh(
                onSwipe = onSwipe,
                hasPreviousArticle = hasPrevious,
                hasNextArticle = hasNext
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
                    ArticleTransition(article = article) { targetArticle ->
                        ArticleReader(
                            article = targetArticle,
                            onSelectMedia = onSelectMedia,
                            onScrollStateReady = onScrollStateReady,
                        )
                    }
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
    focusRequester: FocusRequester,
    isDetailPaneFocused: Boolean,
    onShortcut: (ArticleShortcut) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .articleKeyboardHandler(
                isDetailPaneFocused = isDetailPaneFocused,
                onShortcut = onShortcut,
            )
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
                topBar()
            }

            if (enableBottomBar) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
fun ArticlePullRefresh(
    hasNextArticle: Boolean,
    hasPreviousArticle: Boolean,
    onSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    content: @Composable () -> Unit,
) {
    val (topSwipe, bottomSwipe) = rememberSwipePreferences()
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val enableTopSwipe = topSwipe.enabled &&
            (topSwipe != PREVIOUS_ARTICLE || (topSwipe.openArticle && hasPreviousArticle))

    val enableBottomSwipe = bottomSwipe.enabled &&
            (bottomSwipe != NEXT_ARTICLE || (bottomSwipe.openArticle && hasNextArticle))

    SwipeRefresh(
        onRefresh = { onSwipe(topSwipe) },
        swipeEnabled = enableTopSwipe,
        icon = swipeIcon(
            topSwipe,
            relatedArticleIcon = Icons.Rounded.KeyboardArrowUp
        ),
        onTriggerThreshold = { triggerThreshold() }
    ) {
        SwipeRefresh(
            onRefresh = { onSwipe(bottomSwipe) },
            swipeEnabled = enableBottomSwipe,
            icon = swipeIcon(
                bottomSwipe,
                relatedArticleIcon = Icons.Rounded.KeyboardArrowDown
            ),
            onTriggerThreshold = { triggerThreshold() },
            indicatorAlignment = Alignment.BottomCenter,
        ) {
            content()
        }
    }
}

fun swipeIcon(
    swipe: ArticleVerticalSwipe,
    relatedArticleIcon: ImageVector
): ImageVector {
    return when (swipe) {
        LOAD_FULL_CONTENT -> Icons.AutoMirrored.Rounded.Article
        OPEN_ARTICLE_IN_BROWSER -> Icons.AutoMirrored.Rounded.OpenInNew
        else -> relatedArticleIcon
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
    appPreferences: AppPreferences = koinInject(),
): ToolbarPreferences {
    val pinTopToolbar by appPreferences.readerOptions.pinTopToolbar.stateIn(rememberCoroutineScope())
        .collectAsState()

    val scrollBehavior = if (pinTopToolbar) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior(reverseLayout = true)
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
): () -> Unit {
    val linkOpener = LocalLinkOpener.current

    fun open() {
        val link = article.url?.toString() ?: return

        linkOpener.open(link.toUri())
    }

    return ::open
}

@Stable
private data class SwipePreferences(
    val topSwipe: ArticleVerticalSwipe,
    val bottomSwipe: ArticleVerticalSwipe,
)

private const val SCROLL_PROPORTION = 0.05f
