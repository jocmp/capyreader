package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedScreen(
    viewModel: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    viewModel.feed?.let { feed ->
        EditFeedView(
            feed = feed,
            folders = viewModel.folders,
            feedFoldersTitles = viewModel.feedFolderTitles,
            onSubmit = { entry ->
                viewModel.submit(entry, onSubmit)
            },
            onCancel = onCancel
        )
    }
}
