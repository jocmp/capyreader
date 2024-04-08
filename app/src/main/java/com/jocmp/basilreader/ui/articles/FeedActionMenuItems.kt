package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FeedPreviewFixture

@Composable
fun FeedActionMenuItems(
    feedID: String,
    onMenuClose: () -> Unit,
    onRequestRemove: () -> Unit,
    onEdit: (feedID: String) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_edit))
        },
        onClick = {
            onMenuClose()
            onEdit(feedID)
        }
    )
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_unsubscribe))
        },
        onClick = {
            onMenuClose()
            onRequestRemove()
        }
    )
}

@Preview
@Composable
fun FeedActionMenuPreview() {
    DropdownMenu(expanded = true, onDismissRequest = {}) {
        FeedActionMenuItems(
            feedID = FeedPreviewFixture().values.first().id,
            onMenuClose = {},
            onEdit = {},
            onRequestRemove = {}
        )
    }
}
