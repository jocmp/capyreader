package com.capyreader.app.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val context = LocalContext.current
    LoginView(
        source = viewModel.source,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onUrlChange = viewModel::setURL,
        onClientCertAliasChange = viewModel::setClientCertAlias,
        onSubmit = {
            viewModel.submit(context) {
                onSuccess()
            }
        },
        onNavigateBack = onNavigateBack,
        url = viewModel.url,
        clientCertAlias = viewModel.clientCertAlias,
        username = viewModel.username,
        password = viewModel.password,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage
    )
}
