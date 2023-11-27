package com.jocmp.basilreader.ui.articles

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.FeedFormEntry
import com.jocmp.basil.Folder
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.TextField

@Composable
fun AddFeedView(
    folders: List<Folder>,
    onSubmit: (feed: FeedFormEntry) -> Unit,
    onCancel: () -> Unit
) {
    val (url, setURL) = remember { mutableStateOf("") }
    val (name, setName) = remember { mutableStateOf("") }
    val (addedFolder, setAddedFolder) = remember { mutableStateOf("") }
    val switchFolders = remember {
        folders.map { it.title to false }.toMutableStateMap()
    }

    fun submitFeed() {
        val existingFolderNames = switchFolders.filter { it.value }.keys
        val folderNames = collectFolders(existingFolderNames, addedFolder)

        onSubmit(
            FeedFormEntry(
                url = url,
                name = name,
                folderTitles = folderNames
            )
        )
    }

    Column {
        TextField(
            value = url,
            onValueChange = setURL,
            label = {
                Text(stringResource(id = R.string.add_feed_url_title))
            },
            supportingText = {
                Text(stringResource(R.string.required_placeholder))
            }
        )
        TextField(
            value = name,
            onValueChange = setName,
            label = {
                Text(stringResource(id = R.string.add_feed_name_title))
            }
        )
        TextField(
            value = addedFolder,
            onValueChange = setAddedFolder,
            placeholder = {
                Text(stringResource(id = R.string.add_feed_new_folder_title))
            }
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            switchFolders.forEach { (folderTitle, checked) ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(folderTitle)
                    Switch(
                        checked = checked,
                        onCheckedChange = { value -> switchFolders[folderTitle] = value }
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.add_feed_cancel))
            }
            Button(onClick = { submitFeed() }) {
                Text(stringResource(R.string.add_feed_submit))
            }
        }
    }
}

fun collectFolders(
    existingFolders: Set<String>,
    addedFolder: String
): List<String> {
    val folderNames = existingFolders.toMutableList()

    if (addedFolder.isNotBlank()) {
        folderNames.add(addedFolder)
    }

    return folderNames
}

@Preview
@Composable
fun AddFeedViewPreview() {
    AddFeedView(
        folders = listOf(Folder(title = "Tech")),
        onSubmit = {},
        onCancel = {}
    )
}
