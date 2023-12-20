package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.basil.db.Articles

@Composable
fun ArticleList(
    pager: Pager<Int, Articles>
) {
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(Modifier.fillMaxWidth()) {
        items(count = lazyPagingItems.itemCount) { index ->
            val item = lazyPagingItems[index]
            Column(modifier = Modifier.padding(8.dp)) {
                Text(item?.title ?: "No title", fontSize = 20.sp)

            }
        }
    }
}
