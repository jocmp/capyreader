package com.capyreader.app.ui.articles.feeds.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.DialogHorizontalDivider
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.Folder

@Composable
fun EditFeedView(
    feed: Feed,
    folders: List<Folder>,
    showMultiselect: Boolean,
    onSubmit: (feed: EditFeedFormEntry) -> Unit,
    onCancel: () -> Unit
) {
    val priority = feed.priority

    val feedFolderTitles = folders
        .filter { folder -> folder.feeds.any { it.id == feed.id } }
        .map { it.title }

    fun defaultFolder(): String {
        return if (showMultiselect) {
            ""
        } else {
            feed.folderName
        }
    }

    val scrollState = rememberScrollState()
    val (name, setName) = remember { mutableStateOf(feed.title) }
    val (selectedFolder, setSelectedFolder) = remember { mutableStateOf(defaultFolder()) }
    val switchFolders = remember {
        folders
            .map { it.title to feedFolderTitles.contains(it.title) }
            .toMutableStateMap()
    }

    fun submitFeed() {
        val folderNames = if (showMultiselect) {
            val existingFolderNames = switchFolders.filter { it.value }.keys

            collectFolders(existingFolderNames, selectedFolder)
        } else {
            listOf(selectedFolder)
        }

        onSubmit(
            EditFeedFormEntry(
                feedID = feed.id,
                title = name,
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
                Column {
                    EditFeedURLDisplay(url = feed.feedURL)
                    if (priority != null) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor),
                            headlineContent = {
                                Text(stringResource(R.string.freshrss_visibility))
                            },
                            supportingContent = {
                                Text(stringResource(priority.translationKey))
                            },
                        )
                    }
                }
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

            if (showMultiselect) {
                FormSection(
                    modifier = Modifier.padding(bottom = 16.dp),
                    title = stringResource(R.string.edit_feed_tags_section)
                ) {
                    FolderMultiselect(
                        folders,
                        onUpdateNewFolder = setSelectedFolder,
                        newFolder = selectedFolder,
                        switchFolders = switchFolders,
                        onAddFolder = {
                            if (selectedFolder.isNotBlank()) {
                                switchFolders[selectedFolder] = true
                                setSelectedFolder("")
                            }
                        }
                    )
                }
            } else {
                val (newFolder, setNewFolder) = remember { mutableStateOf("") }

                FormSection(
                    modifier = Modifier.padding(bottom = 16.dp),
                    title = stringResource(R.string.edit_feed_category_section)
                ) {
                    FolderRadioSelect(
                        folders = folders,
                        selectedFolder = selectedFolder,
                        onSelectFolder = setSelectedFolder,
                        newFolder = newFolder,
                        onUpdateNewFolder = setNewFolder,
                        onAddFolder = {
                            if (newFolder.isNotBlank()) {
                                setSelectedFolder(newFolder)
                                setNewFolder("")
                            }
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

@Composable
private fun FolderRadioSelect(
    folders: List<Folder>,
    selectedFolder: String,
    onSelectFolder: (String) -> Unit,
    newFolder: String,
    onUpdateNewFolder: (String) -> Unit,
    onAddFolder: () -> Unit,
) {
    val folderTitles = folders.map { it.title }
    val stagedFolder = selectedFolder.takeIf { it.isNotBlank() && it !in folderTitles }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = newFolder,
            onValueChange = onUpdateNewFolder,
            label = { Text(stringResource(id = R.string.add_feed_new_folder_title)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAddFolder() }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .fillMaxWidth()
        )
        IconButton(
            modifier = Modifier.padding(top = 8.dp, end = 8.dp),
            onClick = onAddFolder,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.blocked_keywords_add_keyword)
            )
        }
    }
    Column {
        AnimatedVisibility(visible = stagedFolder != null) {
            RadioRow(
                title = stagedFolder.orEmpty(),
                selected = true,
                onSelect = {},
            )
        }

        folders.forEach { folder ->
            RadioRow(
                title = folder.title,
                selected = folder.title == selectedFolder,
                onSelect = { onSelectFolder(folder.title) },
            )
        }
    }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
private fun FolderMultiselect(
    folders: List<Folder>,
    onUpdateNewFolder: (title: String) -> Unit,
    newFolder: String,
    onAddFolder: () -> Unit,
    switchFolders: MutableMap<String, Boolean>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = newFolder,
            onValueChange = onUpdateNewFolder,
            label = { Text(stringResource(id = R.string.add_feed_new_folder_title)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAddFolder() }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .fillMaxWidth()
        )
        IconButton(
            modifier = Modifier.padding(top = 8.dp, end = 8.dp),
            onClick = {
                onAddFolder()
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.blocked_keywords_add_keyword)
            )
        }
    }
    Column {
        val addedFolders =
            switchFolders
                .filter { switch -> folders.find { it.title == switch.key } == null }
                .map { it.key }

        addedFolders.forEach { title ->
            val checked = switchFolders.getOrDefault(title, false)

            CheckboxRow(title, checked = checked) { value ->
                switchFolders[title] = value
            }
        }

        folders.forEach { folder ->
            val checked = switchFolders.getOrDefault(folder.title, false)

            CheckboxRow(folder.title, checked = checked) { value ->
                switchFolders[folder.title] = value
            }
        }
    }
}

@Composable
private fun CheckboxRow(title: String, checked: Boolean, onCheck: (value: Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheck(!checked)
            }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheck(it) },
            modifier = Modifier.padding(start = 8.dp)
        )
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

private fun collectFolders(
    existingFolders: Set<String>,
    addedFolder: String,
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
                feed = FeedSample().values.first().copy(priority = FeedPriority.CATEGORY),
                folders = folders,
                showMultiselect = true,
                onSubmit = {},
                onCancel = {}
            )
        }
    }
}

@Preview
@Composable
fun EditFeedViewPreview_SingleSelect() {
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
                folders = folders,
                showMultiselect = false,
                onSubmit = {},
                onCancel = {}
            )
        }
    }
}

private val FeedPriority.translationKey: Int
    get() = when (this) {
        FeedPriority.MAIN_STREAM -> R.string.freshrss_visibility_option_main
        FeedPriority.IMPORTANT -> R.string.freshrss_visibility_option_important
        FeedPriority.CATEGORY -> R.string.freshrss_visibility_option_category
        FeedPriority.FEED -> R.string.freshrss_visibility_option_feed
    }
