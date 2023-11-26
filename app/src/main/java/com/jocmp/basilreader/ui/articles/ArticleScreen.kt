package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
) {
    FeedList(
        folders = viewModel.folders,
        feeds = viewModel.feeds
    )
}
