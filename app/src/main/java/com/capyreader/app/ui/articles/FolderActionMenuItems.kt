package com.capyreader.app.ui.articles

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.Folder
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.FolderPreviewFixture

@Composable
fun FolderActionMenuItems(
    folder: Folder,
    onMenuClose: () -> Unit,
    onRequestRemove: () -> Unit,
    onEdit: (folderTitle: String) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.folder_action_edit))
        },
        onClick = {
            onMenuClose()
            onEdit(folder.title)
        }
    )
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.folder_action_delete_title))
        },
        onClick = {
            onMenuClose()
            onRequestRemove()
        }
    )
}

@Preview
@Composable
fun FolderActionMenuPreview() {
    DropdownMenu(expanded = true, onDismissRequest = {}) {
        FolderActionMenuItems(
            folder = FolderPreviewFixture().values.first(),
            onMenuClose = {},
            onEdit = {},
            onRequestRemove = {},
        )
    }
}
