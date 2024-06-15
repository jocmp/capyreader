package com.jocmp.capyreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.components.TextField
import com.jocmp.capyreader.ui.fixtures.FeedPreviewFixture

@Composable
fun EditFeedView(
    feed: Feed,
    folders: List<Folder>,
    onSubmit: (feed: EditFeedFormEntry) -> Unit,
    onCancel: () -> Unit
) {
    val feedFolderTitles = folders
        .filter { folder -> folder.feeds.any { it.id == feed.id } }
        .map { it.title }

    val scrollState = rememberScrollState()
    val (name, setName) = remember { mutableStateOf(feed.title) }
    val (addedFolder, setAddedFolder) = remember { mutableStateOf("") }
    val switchFolders = remember(folders) {
        folders
            .map { it.title to feedFolderTitles.contains(it.title) }
            .toMutableStateMap()
    }

    val displaySwitchFolders = switchFolders.toSortedMap(String.CASE_INSENSITIVE_ORDER)

    fun submitFeed() {
        val existingFolderNames = switchFolders.filter { it.value }.keys
        val folderNames = collectFolders(existingFolderNames, addedFolder)

        onSubmit(
            EditFeedFormEntry(
                feedID = feed.id,
                title = name,
                folderTitles = folderNames
            )
        )
    }

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        TextField(
            value = name,
            onValueChange = setName,
            placeholder = { Text(feed.title) },
            label = {
                Text(stringResource(id = R.string.add_feed_name_title))
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = false
            )
        )
        TextField(
            value = addedFolder,
            onValueChange = setAddedFolder,
            placeholder = {
                Text(stringResource(id = R.string.add_feed_new_folder_title))
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = false
            )
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            displaySwitchFolders.forEach { (folderTitle, checked) ->
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
                Text(stringResource(R.string.feed_form_cancel))
            }
            Button(onClick = { submitFeed() }) {
                Text(stringResource(R.string.edit_feed_submit))
            }
        }
    }
}

private fun collectFolders(
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
fun EditFeedViewPreview() {
    EditFeedView(
        feed = FeedPreviewFixture().values.first(),
        folders = listOf(Folder("Tech"), Folder("Gaming")),
        onSubmit = {},
        onCancel = {}
    )
}
