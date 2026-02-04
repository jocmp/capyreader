package com.capyreader.app.ui.accounts

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val activity = LocalActivity.current
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
        onChooseClientCert = {
            activity?.let(viewModel::chooseClientCert)
        },
        onClearClientCert = viewModel::clearClientCert,
        url = viewModel.url,
        username = viewModel.username,
        password = viewModel.password,
        clientCertAlias = viewModel.clientCertAlias,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onUseApiTokenChange = viewModel::updateUseApiToken,
    )
}
