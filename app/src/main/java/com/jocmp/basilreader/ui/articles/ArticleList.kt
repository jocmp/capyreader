package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jocmp.basil.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun ArticleList(
    articles: LazyPagingItems<Article>,
    onSelect: suspend (articleID: String) -> Unit,
    selectedArticleKey: String?,
    listState: LazyListState
) {
    val composableScope = rememberCoroutineScope()

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
            val item = articles[index] ?: return@items

            ArticleRow(
                article = item,
                selected = selectedArticleKey == item.id,
                onSelect = { selectArticle(it) },
            )
        }
    }
}
