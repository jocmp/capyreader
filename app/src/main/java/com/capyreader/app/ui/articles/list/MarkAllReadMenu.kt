package com.capyreader.app.ui.articles.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R

@Composable
fun MarkAllReadMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.action_mark_all_read))
            },
            onClick = {
                onMarkAllRead()
                onDismiss()
            }
        )
    }
}
