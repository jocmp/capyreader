package com.capyreader.ui.accounts

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinInject(),
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
        username = viewModel.username,
        password = viewModel.password,
        loading = viewModel.loading,
        showError = viewModel.showError
    )
}
