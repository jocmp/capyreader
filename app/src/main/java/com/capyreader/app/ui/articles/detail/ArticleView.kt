@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults.exitAlwaysScrollBehavior
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.Media
import com.capyreader.app.common.openLink
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe.DISABLED
import com.capyreader.app.preferences.ArticleVerticalSwipe.LOAD_FULL_CONTENT
import com.capyreader.app.preferences.ArticleVerticalSwipe.NEXT_ARTICLE
import com.capyreader.app.preferences.ArticleVerticalSwipe.OPEN_ARTICLE_IN_BROWSER
import com.capyreader.app.preferences.ArticleVerticalSwipe.PREVIOUS_ARTICLE
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    articles: LazyPagingItems<Article>,
//    pagination: ArticlePagination,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onScrollToArticle: (index: Int) -> Unit,
    onSelectArticle: (index: Int, id: String) -> Unit,
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

    val onSwipe = { swipe: ArticleVerticalSwipe ->
        when (swipe) {
            LOAD_FULL_CONTENT -> onToggleFullContent()
            OPEN_ARTICLE_IN_BROWSER -> openLink()
//            PREVIOUS_ARTICLE -> pagination.selectPrevious()
//            NEXT_ARTICLE -> pagination.selectNext()
            DISABLED -> {}
            else -> {}
        }
    }

    val topToolbarPreference = rememberTopToolbarPreference(articleID = article.id)
    val bottomScrollBehavior = exitAlwaysScrollBehavior()
    val enableBottomBar by rememberBottomBarPreference()
    val index = remember(article.id, articles.itemCount) { articles.itemSnapshotList.indexOfFirst { it?.id == article.id } }

    val hasPrevious = index - 1 > -1 && articles[index - 1] != null
    val hasNext = index + 1 < articles.itemCount && articles[index + 1] != null

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
            BottomAppBar(
                scrollBehavior = bottomScrollBehavior,
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.weight(1f),
                ) {
                    ArticleActions(
                        article = article,
                        onToggleExtractContent = onToggleFullContent,
                        onToggleRead = onToggleRead,
                        onToggleStar = onToggleStar,
                    )
                }
            }
        },
        reader = {
            ArticlePullRefresh(
                topToolbarPreference.show && !topToolbarPreference.pinned,
                onSwipe = onSwipe,
                hasPreviousArticle = hasPrevious,
                hasNextArticle = hasNext
            ) {
                HorizontalReaderPager(
                    enabled = enableHorizontalPager,
                    enablePrevious = hasPrevious,
                    enableNext = hasNext,
                    onSelectPrevious = {
                        val idx = index - 1
                        articles[idx]?.let {
                            onSelectArticle(idx, it.id)
                        }
                    },
                    onSelectNext = {
                        val idx = index + 1

                        articles[idx]?.let {
                            onSelectArticle(idx, it.id)
                        }
                    },
                ) {
                    key(article.id) {
                        ArticleReader(
                            article = article,
                            onSelectMedia = onSelectMedia,
                        )
                    }
                }
            }
        },
    )

    LaunchedEffect(index, articles.itemCount) {
        CapyLog.info("index", mapOf("index" to index, "count" to articles.itemCount))
        if (index > -1) {
            onScrollToArticle(index)
        }

        delay(200)
        articles[index + 1]?.let {
            onSelectArticle(index + 1, it.id)
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
    includePadding: Boolean,
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
            icon = swipeIcon(
                bottomSwipe,
                relatedArticleIcon = Icons.Rounded.KeyboardArrowDown
            ),
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

private val TopBarOffset = 56.dp

private val BottomBarOffset = 44.dp

@Composable
fun rememberBottomBarPreference(appPreferences: AppPreferences = koinInject()): State<Boolean> {
    return appPreferences.readerOptions.bottomBarActions
        .stateIn(rememberCoroutineScope())
        .collectAsState()
}

@Composable
fun rememberTopToolbarPreference(
    articleID: String,
    appPreferences: AppPreferences = koinInject(),
): ToolbarPreferences {
    val topBarState = rememberSaveable(articleID, saver = TopAppBarState.Saver) {
        TopAppBarState(0f, 0f, 0f)
    }

    val pinTopToolbar by appPreferences.readerOptions.pinTopToolbar.stateIn(rememberCoroutineScope())
        .collectAsState()

    val scrollBehavior = if (pinTopToolbar) {
        TopAppBarDefaults.pinnedScrollBehavior(state = topBarState)
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior(state = topBarState)
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
