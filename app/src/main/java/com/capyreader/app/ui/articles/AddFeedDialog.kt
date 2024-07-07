package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AddFeedDialog(
    viewModel: AddFeedStateHolder = koinInject(),
    onCancel: () -> Unit,
    onComplete: (feedID: String) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onCancel) {
        Column {
            AddFeedView(
                feedChoices = viewModel.feedChoices,
                onAddFeed = { url ->
                    scope.launch {
                        viewModel.addFeed(
                            url = url,
                            onComplete = { onComplete(it.id) },
                        )
                    }
                },
                onCancel = onCancel,
                loading = viewModel.loading,
                error = viewModel.error
            )
        }
    }
}
