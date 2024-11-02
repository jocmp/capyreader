package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    LoginView(
        source = viewModel.source,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onUrlChange = viewModel::setURL,
        onSubmit = {
            viewModel.submit {
                onSuccess()
            }
        },
        onNavigateBack = onNavigateBack,
        url = viewModel.url,
        username = viewModel.username,
        password = viewModel.password,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage
    )
}
