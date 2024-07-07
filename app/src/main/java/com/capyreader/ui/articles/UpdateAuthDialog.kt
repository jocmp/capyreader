package com.capyreader.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jocmp.capy.Account
import com.capyreader.R
import com.capyreader.ui.components.DialogCard
import com.capyreader.ui.accounts.LoginViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun UpdateAuthDialog(
    onDismissRequest: () -> Unit,
    onSuccess: (message: String) -> Unit,
    account: Account = koinInject(),
    viewModel: LoginViewModel = koinInject(parameters = { parametersOf(account) })
) {
    val successMessage = stringResource(R.string.update_auth_success_message)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogCard {
            UpdateAuthView(
                onPasswordChange = viewModel::setPassword,
                onNavigateBack = onDismissRequest,
                onSubmit = {
                    viewModel.submit {
                        onSuccess(successMessage)
                    }
                },
                username = viewModel.username,
                password = viewModel.password,
                loading = viewModel.loading,
                showError = viewModel.showError
            )
        }
    }
}
