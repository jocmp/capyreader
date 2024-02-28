package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddFeedScreen(
    viewModel: AccountViewModel = koinViewModel(),
) {
    Column {
        AddFeedView(
            onAddFeed = {url ->
                viewModel.addFeed(
                    url = url,
                    onComplete = {},
                )
            }
        )
    }
}
