package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddFeedScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Column {
        AddFeedView(
            folders = viewModel.folders,
            onSubmit = { entry ->
                viewModel.addFeed(
                    entry,
                    onSubmit,
                    onFailure = { message ->
                        scope.launch {
                            snackbarState.showSnackbar(message)
                        }
                    }
                )
            },
            onCancel = onCancel
        )
        SnackbarHost(hostState = snackbarState)
    }
}
