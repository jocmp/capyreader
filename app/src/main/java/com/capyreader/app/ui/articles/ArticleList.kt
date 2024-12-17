package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
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
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState,
    appPreferences: AppPreferences = koinInject(),
) {
    val composableScope = rememberCoroutineScope()
    val articleOptions = rememberArticleOptions()
    val currentTime = rememberCurrentTime()
    val localDensity = LocalDensity.current
    var listHeight by remember { mutableStateOf(0.dp) }
    val enableMarkReadOnScroll by appPreferences.articleListOptions.markReadOnScroll.collectChangesWithDefault()

    val selectArticle = { articleID: String ->
        composableScope.launch {
            onSelect(articleID)
        }
    }

    LazyScrollbar(state = listState) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    listHeight = with(localDensity) { coordinates.size.height.toDp() }
                }
        ) {
            items(count = articles.itemCount) { index ->
                val item = articles[index]

                Box {
                    if (item == null) {
                        PlaceholderArticleRow(articleOptions.imagePreview)
                    } else {
                        ArticleRow(
                            article = item,
                            selected = selectedArticleKey == item.id,
                            onSelect = { selectArticle(it) },
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
            }
        }
    }

    MarkReadOnScroll(
        enabled = enableMarkReadOnScroll,
        articles = articles,
        listState
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
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    onRead: (range: MarkRead) -> Unit
) {
    if (!enabled) {
        return
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(500)
            .collect { firstVisibleIndex ->
                val index = firstVisibleIndex - 1

                if (index < 0 || articles.itemCount == 0) {
                    return@collect
                }

                articles[index]?.let { onRead(MarkRead.After(it.id)) }
            }
    }
}

@Composable
fun rememberCurrentTime(): LocalDateTime {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(30_000)
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

    return ArticleRowOptions(
        showSummary = showSummary,
        showIcon = showIcon,
        showFeedName = showFeedName,
        imagePreview = imagePreview,
        fontScale = fontScale,
    )
}
