package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.jocmp.basil.Account
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onFeedAdd: () -> Unit,
    onFeedSelect: (feedID: String) -> Unit
) {
    Column {
        FeedList(
            folders = viewModel.folders,
            feeds = viewModel.feeds,
            onFeedAdd = onFeedAdd,
            onFeedSelect = onFeedSelect
        )
    }
}
