package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFolderScreen(
    viewModel: EditFolderViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    EditFolderView(
        folder = viewModel.folder,
        onSubmit = {
            viewModel.submit(
                form = it,
                onSubmit = onSubmit
            )
        },
        onCancel = onCancel,
    )
}
