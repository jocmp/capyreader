package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddFeedDialog(
    viewModel: AddFeedViewModel = koinViewModel(),
    onComplete: (feedTitle: String) -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        Column {
            AddFeedView(
                feedChoices = viewModel.feedChoices,
                onAddFeed = { url ->
                    viewModel.addFeed(
                        url = url,
                        onComplete = onComplete,
                    )
                },
                onCancel = onCancel,
                loading = viewModel.loading,
            )
        }
    }
}
