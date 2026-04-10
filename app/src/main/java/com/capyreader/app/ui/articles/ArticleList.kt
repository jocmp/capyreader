package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
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
    dimReadArticles: Boolean = true,
) {
    val articleOptions = rememberArticleOptions().copy(
        dim = dimReadArticles,
    )
    val currentTime = rememberCurrentTime()

    key(listState) {
        LazyScrollbar(state = listState) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
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

                item {
                    Spacer(Modifier.height(120.dp))
                }
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
