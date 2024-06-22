package com.jocmp.capyreader.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
