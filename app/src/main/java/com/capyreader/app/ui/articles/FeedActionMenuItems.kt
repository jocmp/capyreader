package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R

@Composable
fun FeedActionMenuItems(
    onMenuClose: () -> Unit,
    onRemoveRequest: () -> Unit,
    onEdit: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_edit))
        },
        onClick = {
            onMenuClose()
            onEdit()
        }
    )
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_unsubscribe))
        },
        onClick = {
            onMenuClose()
            onRemoveRequest()
        }
    )
}

@Preview
@Composable
fun FeedActionMenuPreview() {
    Column {
        FeedActionMenuItems(
            onMenuClose = {},
            onEdit = {},
            onRemoveRequest = {}
        )
    }
}
