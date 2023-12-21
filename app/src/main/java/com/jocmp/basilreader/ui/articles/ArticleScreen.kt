package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onFeedAdd: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ArticleLayout(
        drawerState = drawerState,
        list = {
            FeedList(
                folders = viewModel.folders,
                feeds = viewModel.feeds,
                onFeedAdd = onFeedAdd,
                onFeedSelect = {
                    viewModel.selectFeed(it) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                }
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            viewModel.articles()?.let { pager ->
                ArticleList(
                    pager = pager,
                    article = viewModel.article,
                    goBack = viewModel::clearArticle,
                    onSelect = {
                        viewModel.selectArticle(it)
                    }
                )
            }
        }
    }
}
