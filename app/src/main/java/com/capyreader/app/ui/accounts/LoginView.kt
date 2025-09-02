package com.capyreader.app.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.titleKey
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.components.Spacing
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.widthMaxSingleColumn
import com.jocmp.capy.accounts.Source
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    source: Source,
    onUsernameChange: (username: String) -> Unit = {},
    onPasswordChange: (password: String) -> Unit = {},
    onUrlChange: (url: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onChooseClientCert: () -> Unit = {},
    onClearClientCert: () -> Unit = {},
    url: String,
    username: String,
    password: String,
    clientCertAlias: String,
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
                        if (source.hasCustomURL) {
                            UrlField(
                                onChange = onUrlChange,
                                url = url,
                                placeholder = {
                                    if (source == Source.FRESHRSS) {
                                        Text(stringResource(R.string.auth_fields_api_url_placeholder))
                                    }
                                }
                            )
                        }
                        AuthFields(
                            onUsernameChange = onUsernameChange,
                            onPasswordChange = onPasswordChange,
                            onSubmit = onSubmit,
                            username = username,
                            password = password,
                            loading = loading,
                            errorMessage = errorMessage,
                            prompt = {
                                ServiceSignup(source)
                            },
                            source = source,
                            onChooseClientCert = onChooseClientCert,
                            onClearClientCert = onClearClientCert,
                            clientCertAlias = clientCertAlias,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UrlField(
    onChange: (url: String) -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
    url: String,
) {
    TextField(
        value = url,
        onValueChange = onChange,
        singleLine = true,
        placeholder = placeholder,
        label = {
            Text(stringResource(R.string.auth_fields_api_url))
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Uri,
        ),
        modifier = Modifier
            .fillMaxWidth()
    )
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
            source = Source.FEEDBIN,
            url = "",
            clientCertAlias = "",
            username = "test@example.com",
            password = "",
        )
    }
}
