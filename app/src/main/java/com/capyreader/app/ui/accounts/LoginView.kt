package com.capyreader.app.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.Spacing
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.ui.components.widthMaxSingleColumn
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = Modifier.safeEdgePadding(),
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .widthMaxSingleColumn()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = Spacing.topBarHeight)
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
