package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: suspend (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState
) {
    val composableScope = rememberCoroutineScope()
    val articleOptions = rememberArticleOptions()
    val currentTime = cachedCurrentTime()

    val selectArticle = { articleID: String ->
        composableScope.launch {
            onSelect(articleID)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id },
        ) { index ->
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
    }
}

@Composable
fun rememberArticleOptions(appPreferences: AppPreferences = koinInject()): ArticleRowOptions {
    val scope = rememberCoroutineScope()

    val showSummary by appPreferences.articleDisplay.showSummary.stateIn(scope).collectAsState()
    val showIcon by appPreferences.articleDisplay.showFeedIcons.stateIn(scope).collectAsState()
    val showFeedName by appPreferences.articleDisplay.showFeedName.stateIn(scope).collectAsState()
    val imagePreview by appPreferences.articleDisplay.imagePreview.stateIn(scope).collectAsState()

    return ArticleRowOptions(
        showSummary = showSummary,
        showIcon = showIcon,
        showFeedName = showFeedName,
        imagePreview = imagePreview
    )
}

@Composable
fun cachedCurrentTime(): LocalDateTime {
    val (currentTime, setCurrentTime) = remember { mutableStateOf(LocalDateTime.now()) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            while (true) {
                delay(30_000)
                setCurrentTime(LocalDateTime.now())
            }
        }

        onDispose {
            job.cancel()
        }
    }

    return currentTime
}
