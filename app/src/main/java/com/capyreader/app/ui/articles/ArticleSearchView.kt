package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.Flow

@Composable
fun ArticleSearchView(
    showResults: Boolean,
    articles: Flow<PagingData<Article>>,
    selectedArticleID: String?,
    onSelectArticle: (articleID: String) -> Unit,
) {
    val pagingArticles = articles.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Surface(
        Modifier.fillMaxSize()
    ) {
        if (showResults) {
            ArticleList(
                articles = pagingArticles,
                listState = listState,
                selectedArticleKey = selectedArticleID,
                onSelect = onSelectArticle,
            )
        }
    }
}
