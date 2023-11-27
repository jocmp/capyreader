package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddFeedScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onSubmit: (accountID: String) -> Unit,
    onCancel: () -> Unit
) {
    AddFeedView(
        folders = viewModel.folders,
        onSubmit = { entry ->
            viewModel.addFeed(entry) {
                onSubmit(viewModel.account.id)
            }
        },
        onCancel = onCancel
    )
}
