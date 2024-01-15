package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.EditFolderForm
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.fixtures.FolderPreviewFixture

@Composable
fun EditFolderView(
    folder: Folder,
    onSubmit: (form: EditFolderForm) -> Unit,
    onCancel: () -> Unit,
) {
    val (folderTitle, setFolderTitle) = remember { mutableStateOf(folder.title) }

    val submit = {
        onSubmit(
            EditFolderForm(
                existingTitle = folder.title,
                title = folderTitle,
            )
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            TextField(
                value = folderTitle,
                onValueChange = setFolderTitle,
                placeholder = {
                    Text(folder.title)
                }
            )
            Row(
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.feed_form_cancel))
                }
                Button(onClick = submit) {
                    Text(stringResource(R.string.edit_feed_submit))
                }
            }
        }
    }
}

@Preview
@Composable
fun EditFolderViewPreview() {
    EditFolderView(
        folder = FolderPreviewFixture().values.first(),
        onSubmit = {},
        onCancel = {}
    )
}
