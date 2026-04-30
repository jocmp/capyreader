package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: (articleID: String) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    enableMarkReadOnScroll: Boolean = false,
    dimReadArticles: Boolean = true,
    scrollToTop: () -> Unit = {},
    isRefreshing: Boolean = false,
) {
    val articleOptions = rememberArticleOptions().copy(
        dim = dimReadArticles,
    )
    val currentTime = rememberCurrentTime()
    val localDensity = LocalDensity.current
    var listHeight by remember { mutableStateOf(0.dp) }
    var hasNewArticles by remember { mutableStateOf(false) }

    if (!enableMarkReadOnScroll) {
        NewArticleObserver(
            articles = articles,
            listState = listState,
            isRefreshing = isRefreshing,
            onNewArticles = { hasNewArticles = true },
            onScrollToTop = { hasNewArticles = false },
        )
    }

    Box(Modifier.fillMaxSize()) {
        key(listState) {
            LazyScrollbar(state = listState) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            listHeight = with(localDensity) { coordinates.size.height.toDp() }
                        }
                ) {
                    items(count = articles.itemCount, key = articles.itemKey { it.id }) { index ->
                        val item = articles[index]

                        Box(Modifier.animateItem()) {
                            if (item == null) {
                                PlaceholderArticleRow(articleOptions.imagePreview)
                            } else {
                                ArticleRow(
                                    article = item,
                                    index = index,
                                    selected = selectedArticleKey == item.id,
                                    onSelect = {
                                        onSelect(it)
                                    },
                                    onMarkAllRead = onMarkAllRead,
                                    currentTime = currentTime,
                                    options = articleOptions
                                )
                            }
                        }
                    }

                    if (enableMarkReadOnScroll && articles.itemCount > 0) {
                        item {
                            FeedOverScrollBox(height = listHeight)
                        }
                    } else {
                        item {
                            Spacer(Modifier.height(120.dp))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = hasNewArticles,
            enter = slideInVertically(animationSpec = pillEnterSpec()) { -it } +
                    fadeIn(animationSpec = pillEnterSpec()),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            NewArticlesPill(
                onClick = {
                    hasNewArticles = false
                    scrollToTop()
                }
            )
        }
    }
}

private fun <T> pillEnterSpec() = tween<T>(delayMillis = 100)

@Composable
fun FeedOverScrollBox(height: Dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Text(
            stringResource(R.string.end_of_feed_text),
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun NewArticleObserver(
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    isRefreshing: Boolean,
    onNewArticles: () -> Unit,
    onScrollToTop: () -> Unit,
) {
    LaunchedEffect(Unit) {
        snapshotFlow { isRefreshing }
            .filter { it }
            .collectLatest {
                val baseline = articles.itemCount

                snapshotFlow { articles.itemCount }
                    .first { it > baseline }

                if (listState.firstVisibleItemIndex > 0) {
                    onNewArticles()
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (index == 0) {
                    onScrollToTop()
                }
            }
    }
}

@Composable
fun rememberCurrentTime(): LocalDateTime {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(30 * 60 * 1_000)
        }
    }

    return currentTime
}

@Composable
fun rememberArticleOptions(appPreferences: AppPreferences = koinInject()): ArticleRowOptions {
    val scope = rememberCoroutineScope()

    val showSummary by appPreferences.articleListOptions.showSummary.stateIn(scope).collectAsState()
    val showIcon by appPreferences.articleListOptions.showFeedIcons.stateIn(scope).collectAsState()
    val showFeedName by appPreferences.articleListOptions.showFeedName.stateIn(scope)
        .collectAsState()
    val imagePreview by appPreferences.articleListOptions.imagePreview.stateIn(scope)
        .collectAsState()
    val fontScale by appPreferences.articleListOptions.fontScale.stateIn(scope).collectAsState()
    val shortenTitles by appPreferences.articleListOptions.shortenTitles.stateIn(scope)
        .collectAsState()
    val accentColors by appPreferences.accentColors.stateIn(scope).collectAsState()

    return ArticleRowOptions(
        showSummary = showSummary,
        showIcon = showIcon,
        showFeedName = showFeedName,
        imagePreview = imagePreview,
        fontScale = fontScale,
        shortenTitles = shortenTitles,
        accentColors = accentColors,
    )
}
