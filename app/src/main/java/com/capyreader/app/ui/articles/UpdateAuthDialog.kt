package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capyreader.app.R
import com.capyreader.app.ui.accounts.UpdateLoginViewModel
import com.capyreader.app.ui.components.DialogCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdateAuthDialog(
    onDismissRequest: () -> Unit,
    onSuccess: (message: String) -> Unit,
    viewModel: UpdateLoginViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val successMessage = stringResource(R.string.update_auth_success_message)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogCard {
            UpdateAuthView(
                source = viewModel.source,
                onPasswordChange = viewModel::setPassword,
                onNavigateBack = onDismissRequest,
                onSubmit = {
                    viewModel.submit(context) {
                        onSuccess(successMessage)
                    }
                },
                username = viewModel.username,
                password = viewModel.password,
                loading = viewModel.loading,
                errorMessage = viewModel.errorMessage
            )
        }
    }
}
