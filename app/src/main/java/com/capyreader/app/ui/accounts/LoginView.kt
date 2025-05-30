package com.capyreader.app.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.titleKey
import com.capyreader.app.ui.components.Spacing
import com.capyreader.app.ui.components.UrlField
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.widthMaxSingleColumn
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    source: Source,
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit = {},
    onUrlChange: (url: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    url: String,
    username: String,
    password: String,
    loading: Boolean = false,
    errorMessage: String? = null,
) {
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
                        text = stringResource(source.titleKey),
                        style = typography.headlineMedium,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AccountAuthFields(
                            username = username,
                            password = password,
                            onUsernameChange = onUsernameChange,
                            onPasswordChange = onPasswordChange,
                            onSubmit = onSubmit,
                            loading = loading,
                            errorMessage = errorMessage,
                            source = source,
                            prompt = {
                                ServiceSignup(source)
                            },
                            urlField = {
                                if (source.hasCustomURL) {
                                    UrlField(
                                        onChange = onUrlChange,
                                        value = url,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginViewPreview() {
    LoginView(
        source = Source.FEEDBIN,
        url = "",
        username = "test@example.com",
        password = "",
    )
}


@Preview
@Composable
private fun LoginViewFreshRSSPreview() {
    LoginView(
        source = Source.FRESHRSS,
        url = "",
        username = "test@example.com",
        password = "",
    )
}
