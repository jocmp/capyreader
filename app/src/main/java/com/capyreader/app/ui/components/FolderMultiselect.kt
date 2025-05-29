package com.capyreader.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Folder

@Composable
fun FolderMultiselect(
    onAdd: (title: String) -> Unit,
    folders: List<Folder>,
    switchFolders: MutableMap<String, Boolean>,
    items: List<String>,
) {
    val containerColor = CardDefaults.cardColors().containerColor

    val (text, updateText) = remember { mutableStateOf("") }

    val add = {
        if (text.isNotBlank()) {
            onAdd(text)
            updateText("")
        }
    }

    RowItem {
        OutlinedTextField(
            value = text,
            onValueChange = updateText,
            label = { Text(stringResource(id = R.string.add_feed_new_folder_title)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false
            ),
            trailingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .heightIn(min = 200.dp)
    ) {
        folders.forEach { folder ->
            val checked = switchFolders.getOrDefault(folder.title, false)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        switchFolders[folder.title] = !checked
                    }
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { switchFolders[folder.title] = it },
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = folder.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
    HorizontalDivider()
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 8.dp)
    ) {
        OutlinedTextField(
            value = text,
            singleLine = true,
            onValueChange = updateText,
            placeholder = { Text(stringResource(R.string.add_feed_new_folder_title)) },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Default,
            ),
            keyboardActions = KeyboardActions(
                onAny = {
                    add()
                }
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        IconButton(
            onClick = {
                add()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_feed_new_folder_title)
            )
        }
    }
}

@Preview
@Composable
fun MultiselectPreview() {
//    val keywords = remember { mutableSetOf("Tech", "News") }

    CapyTheme {
        FolderMultiselect(
            onAdd = {

            },
            onRemove = {

            },
            folders = folders
        )
    }
}
