package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onNewFeedNavigate: (accountID: String) -> Unit,
) {
    Column {
        FeedList(
            folders = viewModel.folders,
            feeds = viewModel.feeds,
            onNewFeedNavigate = { onNewFeedNavigate(viewModel.account.id) }
        )
    }
}
