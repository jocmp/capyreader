package com.jocmp.capyreader.ui.accounts

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
fun AddAccountScreen(
    onAddSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AddAccountViewModel = koinInject()
) {
    AddAccountView(
        onSelectLocal = {
            viewModel.addLocalAccount()
            onAddSuccess()
        },
        onSelectFeedbin = onNavigateToLogin
    )
}
