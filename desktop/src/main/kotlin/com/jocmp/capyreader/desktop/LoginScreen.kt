package com.jocmp.capyreader.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    appState: AppState,
    onAccountCreated: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedSource by remember { mutableStateOf(Source.LOCAL) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var sourceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Capy Reader",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Add an account to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.width(400.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = sourceExpanded,
                onExpandedChange = { sourceExpanded = it },
            ) {
                OutlinedTextField(
                    value = sourceLabel(selectedSource),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = sourceExpanded,
                    onDismissRequest = { sourceExpanded = false },
                ) {
                    Source.entries.forEach { source ->
                        DropdownMenuItem(
                            text = { Text(sourceLabel(source)) },
                            onClick = {
                                selectedSource = source
                                sourceExpanded = false
                            },
                        )
                    }
                }
            }

            if (selectedSource.hasCustomURL) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (selectedSource != Source.LOCAL && selectedSource.requiresUsername) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (selectedSource != Source.LOCAL) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (selectedSource == Source.MINIFLUX_TOKEN) "API Token" else "Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            loading = true
                            error = null

                            scope.launch {
                                try {
                                    val accountID = if (selectedSource == Source.LOCAL) {
                                        appState.manager.createAccount(source = Source.LOCAL)
                                    } else {
                                        val credentials = withContext(Dispatchers.IO) {
                                            Credentials.from(
                                                source = selectedSource,
                                                username = username,
                                                password = password,
                                                url = url,
                                            ).verify().getOrThrow()
                                        }

                                        appState.manager.createAccount(
                                            username = credentials.username,
                                            password = credentials.secret,
                                            url = credentials.url,
                                            source = credentials.source,
                                        )
                                    }

                                    appState.saveAccountID(accountID)
                                    onAccountCreated()
                                } catch (e: Exception) {
                                    error = e.message ?: "Login failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                    ) {
                        Text(if (selectedSource == Source.LOCAL) "Create" else "Sign In")
                    }
                }
            }
        }
    }
}

private fun sourceLabel(source: Source): String {
    return when (source) {
        Source.LOCAL -> "Local (no sync)"
        Source.FEEDBIN -> "Feedbin"
        Source.FRESHRSS -> "FreshRSS"
        Source.MINIFLUX -> "Miniflux"
        Source.MINIFLUX_TOKEN -> "Miniflux (API Token)"
        Source.READER -> "Google Reader API"
    }
}
