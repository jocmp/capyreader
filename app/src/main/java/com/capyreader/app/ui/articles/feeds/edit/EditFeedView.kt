package com.capyreader.app.ui.articles.feeds.edit

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
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
            return ""
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
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    FolderSelect(
                        onChange = setSelectedFolder,
                        value = selectedFolder,
                        options = folders.map { it.title }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderSelect(
    options: List<String>,
    onChange: (value: String) -> Unit,
    value: String,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .fillMaxWidth(),
            value = value,
            onValueChange = onChange,
            label = { Text(stringResource(R.string.edit_feed_tag_section)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(text = option)
                    },
                    onClick = {
                        onChange(option)
                        setExpanded(false)
                    }
                )
            }
        }
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
