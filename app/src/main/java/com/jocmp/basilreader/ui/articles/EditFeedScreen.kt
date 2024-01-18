package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedScreen(
    viewModel: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    EditFeedView(
        feed = viewModel.feed,
        folders = viewModel.folders,
        feedFoldersTitles = viewModel.feedFolderTitles,
        onSubmit = { entry ->
            viewModel.submit(entry, onSubmit)
        },
        onCancel = onCancel
    )
}
