package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchIO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelBottomSheet(
    articleID: String,
    savedSearches: List<SavedSearch>,
    articleLabels: List<String>,
    onAddLabel: (savedSearchID: String) -> Unit,
    onRemoveLabel: (savedSearchID: String) -> Unit,
    onCreateLabel: suspend (articleID: String, name: String) -> Result<String>,
    onDismissRequest: () -> Unit,
) {
    val selectedLabels = remember(articleLabels) {
        mutableStateListOf<String>().apply { addAll(articleLabels) }
    }
    var showCreateDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        val dividerAlpha by remember {
            derivedStateOf {
                if (listState.firstVisibleItemIndex > 0) {
                    1f
                } else {
                    (listState.firstVisibleItemScrollOffset / 100f).coerceIn(0f, 1f)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.freshrss_labels_sheet_title),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { showCreateDialog = true }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.freshrss_labels_new_label))
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.alpha(dividerAlpha))
        LazyColumn(state = listState) {
            items(savedSearches, key = { it.id }) { savedSearch ->
                val isSelected = selectedLabels.contains(savedSearch.id)

                LabelRow(
                    name = savedSearch.name,
                    isSelected = isSelected,
                    onToggle = {
                        if (isSelected) {
                            selectedLabels.remove(savedSearch.id)
                            onRemoveLabel(savedSearch.id)
                        } else {
                            selectedLabels.add(savedSearch.id)
                            onAddLabel(savedSearch.id)
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateLabelDialog(
            onDismiss = { showCreateDialog = false },
            onCreateLabel = { name ->
                onCreateLabel(articleID, name)
            },
            onLabelApplied = { labelID ->
                selectedLabels.add(labelID)
            },
        )
    }
}

@Composable
private fun CreateLabelDialog(
    onDismiss: () -> Unit,
    onCreateLabel: suspend (name: String) -> Result<String>,
    onLabelApplied: (savedSearchID: String) -> Unit,
) {
    var labelName by remember { mutableStateOf("") }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun save() {
        val name = labelName.trim()

        if (name.isBlank()) {
            return
        }

        scope.launchIO {
            onCreateLabel(name).fold(
                onSuccess = { labelID ->
                    onLabelApplied(labelID)
                    onDismiss()
                },
                onFailure = { error ->
                    setErrorMessage(error.message ?: "Failed to create label")
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.freshrss_labels_create_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = labelName,
                    onValueChange = {
                        labelName = it
                        setErrorMessage(null)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { save() }
                    ),
                    label = { Text(stringResource(R.string.freshrss_labels_create_dialog_label_name)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    save()
                },
                enabled = labelName.isNotBlank()
            ) {
                Text(stringResource(R.string.freshrss_labels_create_dialog_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun LabelRow(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
