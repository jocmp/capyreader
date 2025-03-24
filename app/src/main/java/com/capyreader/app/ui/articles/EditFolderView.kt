package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.jocmp.capy.EditFolderFormEntry

@Composable
fun EditFolderView(
    folderTitle: String,
    onSubmit: (form: EditFolderFormEntry) -> Unit,
    onCancel: () -> Unit
) {
    val (title, setTitle) = remember { mutableStateOf(folderTitle) }

    val submit = {
        onSubmit(EditFolderFormEntry(previousTitle = folderTitle, folderTitle = title))
    }

    Column(
        Modifier.padding(top = 16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = setTitle,
            placeholder = { Text(folderTitle) },
            label = {
                Text(stringResource(id = R.string.tag_name_title))
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.feed_form_cancel))
            }
            TextButton(onClick = { submit() }) {
                Text(stringResource(R.string.edit_feed_submit))
            }
        }
    }
}
