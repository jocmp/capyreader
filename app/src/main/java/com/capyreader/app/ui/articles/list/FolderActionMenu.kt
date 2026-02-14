package com.capyreader.app.ui.articles.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.EditFolderDialog
import com.capyreader.app.ui.articles.RemoveFolderDialog
import com.capyreader.app.ui.settings.localSnackbarDisplay
import com.jocmp.capy.accounts.Source

@Composable
fun FolderActionMenu(
    onDismissMenuRequest: () -> Unit,
    folderTitle: String,
    onRemoveRequest: (folderTitle: String, completion: (result: Result<Unit>) -> Unit) -> Unit,
    expanded: Boolean,
    source: Source,
) {
    val editSuccessMessage = stringResource(R.string.tag_action_edit_success)
    val editErrorMessage = stringResource(R.string.tag_action_edit_error)
    val deleteErrorMessage = stringResource(R.string.delete_tag_error)
    val showSnackbar = localSnackbarDisplay()

    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val (isEditDialogOpen, setEditDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveComplete = { result: Result<Unit> ->
        if (result.isFailure) {
            showSnackbar(deleteErrorMessage)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissMenuRequest,
    ) {
        val editTitle = if (source == Source.FRESHRSS) {
            R.string.freshrss_tag_action_edit
        } else {
            R.string.tag_action_edit
        }
        DropdownMenuItem(
            text = {
                Text(stringResource(editTitle))
            },
            onClick = {
                onDismissMenuRequest()
                setEditDialogOpen(true)
            }
        )
        if (source.supportsTagDeletion) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.tag_action_delete_title))
                },
                onClick = {
                    onDismissMenuRequest()
                    setRemoveDialogOpen(true)
                }
            )
        }
    }

    if (isRemoveDialogOpen) {
        RemoveFolderDialog(
            folderTitle,
            onConfirm = {
                setRemoveDialogOpen(false)
                onRemoveRequest(folderTitle) {
                    onRemoveComplete(it)
                }
            },
            onDismissRequest = { setRemoveDialogOpen(false) }
        )
    }

    EditFolderDialog(
        folderTitle = folderTitle,
        isOpen = isEditDialogOpen,
        onDismiss = {
            setEditDialogOpen(false)
        }
    ) { result ->
        result.fold(
            onSuccess = {
                showSnackbar(editSuccessMessage)
            },
            onFailure = {
                showSnackbar(editErrorMessage)
            }
        )
    }
}
