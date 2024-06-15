package com.jocmp.capyreader.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.jocmp.capyreader.R

@Composable
fun MarkAllReadButton(
    onMarkAllRead: () -> Unit
) {
    val (isDialogOpen, setDialogOpen) = remember {
        mutableStateOf(false)
    }

    val closeDialog = {
        setDialogOpen(false)
    }

    IconButton(onClick = { setDialogOpen(true) }) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.action_mark_all_read)
        )
    }

    if (isDialogOpen) {
        MarkAllReadDialog(
            onConfirm = {
                closeDialog()
                onMarkAllRead()
            },
            onDismissRequest = { closeDialog() }
        )
    }
}
