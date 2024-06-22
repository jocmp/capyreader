package com.jocmp.capyreader.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.components.DialogCard
import org.koin.compose.koinInject

@Composable
fun UpdateAuthDialog(
    onDismissRequest: () -> Unit,
    onSuccess: (message: String) -> Unit,
    viewModel: UpdateAuthViewModel = koinInject()
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
