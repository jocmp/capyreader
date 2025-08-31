package com.capyreader.app.ui.articles

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: (articleID: String) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    refreshingAll: Boolean,
    enableMarkReadOnScroll: Boolean = false,
) {
    val articleOptions = rememberArticleOptions()
    val currentTime = rememberCurrentTime()
    val localDensity = LocalDensity.current
    var listHeight by remember { mutableStateOf(0.dp) }

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

                Box {
                    if (item == null) {
                        PlaceholderArticleRow(articleOptions.imagePreview)
                    } else {
                        ArticleRow(
                            article = item,
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

    MarkReadOnScroll(
        enabled = enableMarkReadOnScroll,
        refreshingAll = refreshingAll,
        articles = articles,
        listState = listState
    ) { range ->
        onMarkAllRead(range)
    }
}

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

@OptIn(FlowPreview::class)
@Composable
fun MarkReadOnScroll(
    enabled: Boolean,
    refreshingAll: Boolean,
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    onRead: (range: MarkRead) -> Unit
) {
    if (!enabled) {
        return
    }

    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(firstVisibleIndex, refreshingAll) {
        val offscreenIndex = firstVisibleIndex - 1
        val markAsRead = !refreshingAll && offscreenIndex > 1
        CapyLog.info(
            "index",
            mapOf(
                "index" to firstVisibleIndex,
                "refreshing" to refreshingAll,
                "markRead" to markAsRead
            )
        )

        if (refreshingAll && !listState.isScrollInProgress) {
            CapyLog.info("scroll", mapOf("index" to firstVisibleIndex))
            listState.scrollToItem(0)
        } else {
            CapyLog.info("scrollSkip", mapOf("index" to firstVisibleIndex))
        }

        if (markAsRead) {
            articles.getOrNull(offscreenIndex)?.let { onRead(MarkRead.After(it.id)) }
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

    return ArticleRowOptions(
        showSummary = showSummary,
        showIcon = showIcon,
        showFeedName = showFeedName,
        imagePreview = imagePreview,
        fontScale = fontScale,
        shortenTitles = shortenTitles,
    )
}

private fun <T : Any> LazyPagingItems<T>.getOrNull(index: Int): T? {
    return if (index in 0..<itemCount) get(index) else null
}
