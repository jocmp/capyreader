package com.capyreader.app.ui.articles

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: suspend (articleID: String) -> Unit,
    onMarkRead: (markRead: MarkRead) -> Unit,
    onToggleRead: (articleID: String) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState,
) {
    val composableScope = rememberCoroutineScope()
    val articleOptions = rememberArticleOptions()
    val currentTime = remember { LocalDateTime.now() }

    val selectArticle = { articleID: String ->
        composableScope.launch {
            onSelect(articleID)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id },
        ) { index ->
            val item = articles[index]
            val dismissState = rememberSwipeToDismissBoxState()

            SwipeToDismissBox(
                state = dismissState,
                gesturesEnabled = item != null,
                backgroundContent = {
                    val color by animateColorAsState(
                        targetValue =
                        when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.Settled -> colorScheme.surfaceContainerLow
                            SwipeToDismissBoxValue.StartToEnd -> Color.Blue
                            SwipeToDismissBoxValue.EndToStart -> Color.Blue
                        },
                        label = ""
                    )

                    Box(Modifier.fillMaxSize().background(color))
                }
            ) {
                if (item == null) {
                    PlaceholderArticleRow(articleOptions.imagePreview)
                } else {
                    ArticleRow(
                        article = item,
                        selected = selectedArticleKey == item.id,
                        onSelect = { selectArticle(it) },
                        onMarkAllRead = onMarkRead,
                        currentTime = currentTime,
                        options = articleOptions
                    )

                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        LaunchedEffect(Unit) {
                            onToggleRead(item.id)
                            dismissState.reset()
                        }
                    }
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
