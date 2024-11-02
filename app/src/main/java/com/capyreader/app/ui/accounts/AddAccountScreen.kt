package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import com.jocmp.capy.accounts.Source
import org.koin.compose.koinInject

@Composable
fun AddAccountScreen(
    onAddSuccess: () -> Unit,
    onNavigateToLogin: (source: Source) -> Unit,
    viewModel: AddAccountViewModel = koinInject()
) {
    AddAccountView(
        onSelectLocal = {
            viewModel.addLocalAccount()
            onAddSuccess()
        },
        onSelectService = onNavigateToLogin
    )
}
