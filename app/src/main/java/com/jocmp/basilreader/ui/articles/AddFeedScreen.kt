package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.ui.accounts.AccountViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddFeedScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    AddFeedView(
        folders = viewModel.folders,
        onSubmit = { entry ->
            viewModel.addFeed(entry, onSubmit)
        },
        onCancel = onCancel
    )
}
