package com.jocmp.capyreader.desktop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddFeedDialog(
    state: ReaderState,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var choices by remember { mutableStateOf<List<FeedOption>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
            ) {
                Text(
                    text = if (choices != null) "Choose a feed" else "Add Feed",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(16.dp))

                if (choices != null) {
                    LazyColumn {
                        items(choices.orEmpty()) { option ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        loading = true
                                        error = null
                                        scope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                state.account.addFeed(url = option.feedURL)
                                            }
                                            loading = false
                                            when (result) {
                                                is AddFeedResult.Success -> {
                                                    state.loadArticles()
                                                    onDismiss()
                                                }
                                                is AddFeedResult.Failure -> {
                                                    error = feedErrorMessage(result.error)
                                                }
                                                is AddFeedResult.MultipleChoices -> {
                                                    choices = result.choices
                                                }
                                            }
                                        }
                                    },
                            ) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = option.feedURL,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Feed or site URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (choices != null) {
                        TextButton(onClick = {
                            choices = null
                            error = null
                        }) {
                            Text("Back")
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    if (choices == null) {
                        if (loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    if (url.isBlank()) return@Button
                                    loading = true
                                    error = null

                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            state.account.addFeed(url = url)
                                        }
                                        loading = false
                                        when (result) {
                                            is AddFeedResult.Success -> {
                                                state.loadArticles()
                                                onDismiss()
                                            }
                                            is AddFeedResult.MultipleChoices -> {
                                                choices = result.choices
                                            }
                                            is AddFeedResult.Failure -> {
                                                error = feedErrorMessage(result.error)
                                            }
                                        }
                                    }
                                },
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun feedErrorMessage(error: AddFeedResult.Error): String {
    return when (error) {
        is AddFeedResult.Error.FeedNotFound -> "No feed found at that URL"
        is AddFeedResult.Error.ConnectionError -> "Could not connect to server"
        is AddFeedResult.Error.NetworkError -> "Network error"
        is AddFeedResult.Error.SaveFailure -> "Failed to save feed"
    }
}
