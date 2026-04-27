package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.flow.distinctUntilChanged
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
) {
    val articleOptions = rememberArticleOptions().copy(
        dim = dimReadArticles,
    )
    val currentTime = rememberCurrentTime()
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var listHeight by remember { mutableStateOf(0.dp) }
    var hasNewArticles by remember { mutableStateOf(false) }

    if (!enableMarkReadOnScroll) {
        LaunchedEffect(listState) {
            var lastCount = listState.layoutInfo.totalItemsCount

            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .distinctUntilChanged()
                .collect { count ->
                    if (count > lastCount && listState.firstVisibleItemIndex > 0) {
                        hasNewArticles = true
                    }
                    lastCount = count
                }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { index ->
                    if (index == 0) {
                        hasNewArticles = false
                    }
                }
        }
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
            enter = slideInVertically { -it } + fadeIn(),
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

@Composable
private fun NewArticlesPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(
                PaddingValues(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(stringResource(R.string.article_list_new_articles_pill))
        }
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
