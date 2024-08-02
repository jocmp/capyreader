package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinInject(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    LoginView(
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onSubmit = {
            viewModel.submit {
                onSuccess()
            }
        },
        onNavigateBack = onNavigateBack,
        username = viewModel.username,
        password = viewModel.password,
        loading = viewModel.loading,
        showError = viewModel.showError
    )
}
