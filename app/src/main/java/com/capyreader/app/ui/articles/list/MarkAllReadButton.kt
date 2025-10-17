package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.MarkReadPosition

@Composable
fun MarkAllReadButton(
    onMarkAllRead: () -> Unit,
    position: MarkReadPosition = MarkReadPosition.TOOLBAR,
) {
    val confirmationEnabled by rememberMarkAllReadState()

    val (isDialogOpen, setDialogOpen) = remember {
        mutableStateOf(false)
    }

    val closeDialog = {
        setDialogOpen(false)
    }

    val onClick = {
        if (confirmationEnabled) {
            setDialogOpen(true)
        } else {
            onMarkAllRead()
        }
    }

    if (position == MarkReadPosition.FLOATING_ACTION_BUTTON) {
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            onClick = {
                onClick()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.action_mark_all_read)
            )
        }
    } else {
        IconButton(
            onClick = {
                onClick()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.action_mark_all_read)
            )
        }
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
