package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R

@Composable
fun AddFeedButton(
    onComplete: (feedID: String) -> Unit,
    iconOnly: Boolean = false,
) {
    val (isDialogOpen, setDialogOpen) = rememberSaveable { mutableStateOf(false) }

    val closeDialog = {
        setDialogOpen(false)
    }

    val onClick = { setDialogOpen(true) }

    if (iconOnly) {
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.nav_add_feed)
            )
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Box(Modifier.padding(end = 8.dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(stringResource(R.string.nav_add_feed))
        }
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

@Composable
@Preview
fun AddFeedButtonPreview() {
    AddFeedButton(
        onComplete = {},
        iconOnly = true
    )
}
