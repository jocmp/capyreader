package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.jocmp.basil.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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
            key = lazyPagingItems.itemKey { it.id }
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
