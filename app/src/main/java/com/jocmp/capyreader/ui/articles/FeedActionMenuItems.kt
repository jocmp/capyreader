package com.jocmp.capyreader.ui.articles

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.Feed
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.fixtures.FeedPreviewFixture

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
    DropdownMenu(expanded = true, onDismissRequest = {}) {
        FeedActionMenuItems(
            onMenuClose = {},
            onEdit = {},
            onRemoveRequest = {}
        )
    }
}
