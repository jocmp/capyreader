package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jocmp.basil.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun ArticleList(
    articles: Flow<PagingData<Article>>,
    onSelect: suspend (articleID: String) -> Unit,
    selectedArticleKey: String?,
) {
    val composableScope = rememberCoroutineScope()
    val lazyPagingItems = articles.collectAsLazyPagingItems()

    val selectArticle = { articleID: String ->
        composableScope.launch {
            onSelect(articleID)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.id },
        ) { index ->
            val item = lazyPagingItems[index] ?: return@items

            ArticleRow(
                article = item,
                selected = selectedArticleKey == item.id,
                onSelect = { selectArticle(it) },
            )
        }
    }
}

private fun String.orNullIfBlank(): String? {
    return ifBlank {
        null
    }
}
