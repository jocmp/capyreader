package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.jocmp.basil.FeedSearch
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddFeedScreen(
    viewModel: AccountViewModel = koinViewModel(),
    feedSearch: FeedSearch = koinInject(),
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
            searchFeeds = { feedSearch.search(it).getOrNull() },
            onCancel = onCancel
        )
        SnackbarHost(hostState = snackbarState)
    }
}
