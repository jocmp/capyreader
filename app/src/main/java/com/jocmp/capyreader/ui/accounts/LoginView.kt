package com.jocmp.capyreader.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capyreader.R
import com.jocmp.capyreader.setupCommonModules
import com.jocmp.capyreader.ui.components.widthMaxSingleColumn
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun LoginView(
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    username: String,
    password: String,
    loading: Boolean = false,
    showError: Boolean = false,
) {
    val errorMessage = if (showError) {
        stringResource(R.string.auth_error_message)
    } else {
        null
    }

    Scaffold { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.widthMaxSingleColumn()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = typography.headlineMedium,
                    )
                    AuthFields(
                        username = username,
                        password = password,
                        onUsernameChange = onUsernameChange,
                        onPasswordChange = onPasswordChange,
                        onSubmit = onSubmit,
                        loading = loading,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginViewPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        LoginView(
            username = "test@example.com",
            password = "",
        )
    }
}
