package com.jocmp.capyreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.components.DialogCard
import com.jocmp.capyreader.ui.accounts.AuthFields
import com.jocmp.capyreader.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAuthView(
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

    DialogCard {
        MediumTopAppBar(
            colors = TopAppBarDefaults.mediumTopAppBarColors().copy(
                containerColor = colorScheme.surfaceVariant
            ),
            title = {
                Text(text = stringResource(R.string.update_auth_title))
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
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
            AuthFields(
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit,
                username = username,
                password = password,
                readOnlyUsername = true,
                loading = loading,
                errorMessage = errorMessage
            )
        }
    }
}

@Preview
@Composable
private fun UpdateAuthViewPreview() {
    CapyTheme {
        UpdateAuthView(
            username = "test@example.com",
            password = "secrets"
        )
    }
}
