package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.jocmp.basil.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: suspend (articleID: String) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState
) {
    val composableScope = rememberCoroutineScope()
    val currentTime = cachedCurrentTime()

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

            Box {
                if (item == null) {
                    PlaceholderArticleRow()
                } else {
                    ArticleRow(
                        article = item,
                        selected = selectedArticleKey == item.id,
                        onSelect = { selectArticle(it) },
                        currentTime = currentTime
                    )
                }
            }
        }
    }
}

@Composable
fun cachedCurrentTime(): LocalDateTime {
    val (currentTime, setCurrentTime) = remember { mutableStateOf(LocalDateTime.now()) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            while(true) {
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
