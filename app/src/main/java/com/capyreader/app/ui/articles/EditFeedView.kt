package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.DialogHorizontalDivider
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.settings.RowItem
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder

@Composable
fun EditFeedView(
    feed: Feed,
    allFolders: List<Folder>,
    onSubmit: (feed: EditFeedFormEntry) -> Unit,
    onCancel: () -> Unit
) {
    val folders = remember(allFolders.isEmpty()) {
        allFolders
    }

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

    var enableNotifications by remember { mutableStateOf(feed.enableNotifications) }

    fun submitFeed() {
        val existingFolderNames = switchFolders.filter { it.value }.keys
        val folderNames = collectFolders(existingFolderNames, addedFolder)

        onSubmit(
            EditFeedFormEntry(
                feedID = feed.id,
                title = name,
                enableNotifications = enableNotifications,
                folderTitles = folderNames
            )
        )
    }

    Column {
        Column(
            Modifier
                .weight(0.1f, fill = false)
                .heightIn(max = 500.dp)
                .verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
            ) {
                EditFeedURLDisplay(feedURL = feed.feedURL)
                OutlinedTextField(
                    value = name,
                    onValueChange = setName,
                    placeholder = { Text(feed.title) },
                    label = {
                        Text(stringResource(id = R.string.add_feed_name_title))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            RowItem {
                Box(Modifier.padding(bottom = 16.dp)) {
                    TextSwitch(
                        onCheckedChange = { enableNotifications = it },
                        checked = enableNotifications,
                        title = stringResource(R.string.edit_feed_notifications_title),
                    )
                }
            }
            FormSection(
                modifier = Modifier.padding(bottom = 16.dp),
                title = stringResource(R.string.edit_feed_tags_section)
            ) {
                RowItem {
                    OutlinedTextField(
                        value = addedFolder,
                        onValueChange = setAddedFolder,
                        label = {
                            Text(stringResource(id = R.string.add_feed_new_folder_title))
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = false
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                RowItem {
                    displaySwitchFolders.forEach { (folderTitle, checked) ->
                        TextSwitch(
                            onCheckedChange = { switchFolders[folderTitle] = it },
                            checked = checked,
                            title = {
                                Text(
                                    text = folderTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        DialogHorizontalDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.feed_form_cancel))
            }
            TextButton(onClick = { submitFeed() }) {
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
    val folders = listOf(
        "Tech",
        "Really Long Title With Some Spaces Between Words",
        "ReallyLongTitleWithoutAnyBreaksInTheWords",
        "News",
    ).map {
        Folder(it)
    }

    CapyTheme {
        Card(Modifier.height(600.dp)) {
            EditFeedView(
                feed = FeedSample().values.first(),
                allFolders = folders,
                onSubmit = {},
                onCancel = {}
            )
        }
    }
}
