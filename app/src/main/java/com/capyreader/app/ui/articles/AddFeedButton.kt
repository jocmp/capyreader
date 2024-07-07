package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R

@Composable
fun AddFeedButton(
    onComplete: (feedID: String) -> Unit,
) {
    val (isDialogOpen, setDialogOpen) = rememberSaveable { mutableStateOf(false) }

    val closeDialog = {
        setDialogOpen(false)
    }

    OutlinedButton(
        onClick = { setDialogOpen(true) },
    ) {
        Box(Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(stringResource(R.string.nav_add_feed))
    }

    if (isDialogOpen) {
        AddFeedDialog(
            onCancel = { closeDialog() },
            onComplete = { feedID ->
                closeDialog()
                onComplete(feedID)
            }
        )
    }
}
