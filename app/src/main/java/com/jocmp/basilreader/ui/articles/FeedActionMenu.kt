package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basilreader.R

@Composable
fun FeedActionMenu(
    onRemove: () -> Unit,
    onEdit: () -> Unit,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val dismissRemoveDialog = {
        setRemoveDialogOpen(false)
    }

    val confirmRemoval = {
        setRemoveDialogOpen(false)
        onRemove()
    }

    Box {
        IconButton(onClick = { setExpanded(true) }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.feed_action_manage_button)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.feed_action_unsubscribe))
                },
                onClick = {
                    setExpanded(false)
                    setRemoveDialogOpen(true)
                }
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.feed_action_edit))
                },
                onClick = {
                    setExpanded(false)
                    onEdit()
                }
            )
        }
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = dismissRemoveDialog,
            text = {
                Text(stringResource(R.string.feed_action_unsubscribe_message))
            },
            dismissButton = {
                TextButton(onClick = dismissRemoveDialog) {
                    Text(stringResource(R.string.feed_action_unsubscribe_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = confirmRemoval) {
                    Text(stringResource(R.string.feed_action_unsubscribe_confirm))
                }
            }
        )
    }
}

@Preview
@Composable
fun FeedActionMenuPreview() {
    FeedActionMenu(
        onEdit = {},
        onRemove = {}
    )
}
