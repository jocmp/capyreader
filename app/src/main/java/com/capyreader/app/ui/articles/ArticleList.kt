package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.MaterialTheme.motionScheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.isSinglePane
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: (article: Article) -> Unit,
    selectedArticleKey: String?,
    scrollState: ArticleScrollState,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    enableMarkReadOnScroll: Boolean = false,
    dimReadArticles: Boolean = true,
    showNewArticles: Boolean = false,
    scrollToTop: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    val articleOptions = rememberArticleOptions().copy(
        dim = dimReadArticles,
    )
    val currentTime = rememberCurrentTime()
    val localDensity = LocalDensity.current
    var listHeight by remember { mutableStateOf(0.dp) }
    val articleKey = articles.itemKey { it.id }
    val showOverScroll = enableMarkReadOnScroll && articles.itemCount > 0
    val measureHeight = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            listHeight = with(localDensity) { coordinates.size.height.toDp() }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        key(scrollState) {
            when (scrollState) {
                is ArticleListScrollState -> {
                    ArticleListScrollbar(listState = scrollState.state) {
                        LazyColumn(
                            state = scrollState.state,
                            contentPadding = contentPadding,
                            modifier = measureHeight,
                        ) {
                            items(count = articles.itemCount, key = articleKey) { index ->
                                val item = articles[index]

                                Box(modifier = Modifier.animateItem()) {
                                    if (item == null) {
                                        PlaceholderArticleRow(articleOptions.imagePreview)
                                    } else {
                                        ArticleRow(
                                            article = item,
                                            index = index,
                                            selected = selectedArticleKey == item.id,
                                            onSelect = {
                                                onSelect(item)
                                            },
                                            onMarkAllRead = onMarkAllRead,
                                            currentTime = currentTime,
                                            options = articleOptions
                                        )
                                    }
                                }
                            }

                            item {
                                ListFooter(showOverScroll = showOverScroll, height = listHeight)
                            }
                        }
                    }
                }

                is ArticleGridScrollState -> {
                    LazyVerticalStaggeredGrid(
                        columns = ArticleCardCells,
                        state = scrollState.state,
                        contentPadding = cardContentPadding(contentPadding),
                        verticalItemSpacing = ArticleCardGutter,
                        horizontalArrangement = Arrangement.spacedBy(ArticleCardGutter),
                        modifier = measureHeight,
                    ) {
                        items(count = articles.itemCount, key = articleKey) { index ->
                            val item = articles[index]

                            if (item == null) {
                                PlaceholderArticleCard(
                                    showImage = articleOptions.imagePreview != ImagePreview.NONE,
                                    modifier = Modifier.animateItem(),
                                )
                            } else {
                                ArticleCard(
                                    article = item,
                                    index = index,
                                    selected = selectedArticleKey == item.id,
                                    onSelect = { onSelect(item) },
                                    onMarkAllRead = onMarkAllRead,
                                    currentTime = currentTime,
                                    options = articleOptions,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }

                        item(span = StaggeredGridItemSpan.FullLine) {
                            ListFooter(showOverScroll = showOverScroll, height = listHeight)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showNewArticles,
            enter = slideInVertically(animationSpec = motionScheme.defaultSpatialSpec()) { -it } +
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
            exit = slideOutVertically(animationSpec = motionScheme.fastSpatialSpec()) { -it } +
                fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        ) {
            NewArticlesButton(onClick = scrollToTop)
        }
    }
}

/**
 * In a two-pane layout the list stays beside the reader, so keep the selected article in view
 * (e.g. when stepping next/previous) by scrolling to it when it isn't already visible.
 */
@Composable
fun ScrollToSelectedArticleEffect(
    selectedArticleKey: String?,
    articles: LazyPagingItems<Article>,
    scrollState: ArticleScrollState,
) {
    val isSinglePane = isSinglePane()

    LaunchedEffect(selectedArticleKey, isSinglePane, articles.itemCount, scrollState) {
        if (isSinglePane) return@LaunchedEffect
        val id = selectedArticleKey ?: return@LaunchedEffect
        val index = articles.itemSnapshotList.indexOfFirst { it?.id == id }
        if (index > -1 && scrollState.visibleItems.none { it.index == index }) {
            scrollState.animateScrollToItem(index)
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

@Composable
private fun ListFooter(showOverScroll: Boolean, height: Dp) {
    if (showOverScroll) {
        FeedOverScrollBox(height = height)
    } else {
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun cardContentPadding(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection) + ArticleCardGutter,
        top = contentPadding.calculateTopPadding() + ArticleCardGutter,
        end = contentPadding.calculateEndPadding(layoutDirection) + ArticleCardGutter,
        bottom = contentPadding.calculateBottomPadding() + ArticleCardGutter,
    )
}
