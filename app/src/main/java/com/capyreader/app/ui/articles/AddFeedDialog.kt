package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import com.capyreader.app.ui.components.DialogCard
import org.koin.compose.koinInject

@Composable
fun AddFeedDialog(
    viewModel: AddFeedViewModel = koinInject(),
    onCancel: () -> Unit,
    onComplete: (feedID: String) -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        DialogCard {
            AddFeedView(
                feedChoices = viewModel.feedChoices,
                onAddFeed = { url ->
                    viewModel.addFeed(
                        url = url,
                        onComplete = { onComplete(it.id) },
                    )
                },
                onCancel = onCancel,
                loading = viewModel.loading,
                error = viewModel.error
            )
        }
    }
}
