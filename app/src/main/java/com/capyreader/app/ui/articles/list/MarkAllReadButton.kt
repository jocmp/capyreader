package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R

@Composable
fun MarkAllReadButton(
    onMarkAllRead: () -> Unit
) {
    val confirmationEnabled by rememberMarkAllReadState()

    val (isDialogOpen, setDialogOpen) = remember {
        mutableStateOf(false)
    }

    val closeDialog = {
        setDialogOpen(false)
    }

    FloatingActionButton(
        shape = CircleShape,
        onClick = {
            if (confirmationEnabled) {
                setDialogOpen(true)
            } else {
                onMarkAllRead()
            }
        }
    ) {
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
