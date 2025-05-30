package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.titleKey
import com.capyreader.app.ui.accounts.AccountAuthFields
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAuthView(
    source: Source,
    onPasswordChange: (password: String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    username: String,
    password: String,
    loading: Boolean = false,
    errorMessage: String? = null
) {
    DialogCard {
        MediumTopAppBar(
            colors = TopAppBarDefaults.mediumTopAppBarColors().copy(
                containerColor = colorScheme.surfaceVariant
            ),
            title = {
                Text(text = stringResource(source.titleKey))
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null
                    )
                }
            },
        )
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            AccountAuthFields(
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit,
                username = username,
                password = password,
                readOnlyUsername = true,
                loading = loading,
                errorMessage = errorMessage,
                source = source,
            )
        }
    }
}

@Preview
@Composable
private fun UpdateAuthViewPreview() {
    CapyTheme {
        UpdateAuthView(
            source = Source.FRESHRSS,
            username = "test@example.com",
            password = "secrets"
        )
    }
}
