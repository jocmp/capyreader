package com.jocmp.basilreader.ui.articles

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.AddFeedForm
import com.jocmp.basil.FeedSearch.SearchResult
import com.jocmp.basil.Folder
import com.jocmp.basil.common.orEmpty
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.TextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddFeedView(
    folders: List<Folder>,
    onSubmit: (feed: AddFeedForm) -> Unit,
    onCancel: () -> Unit,
    searchFeeds: suspend (url: String) -> SearchResult?,
) {
    val scope = rememberCoroutineScope()
    val (queryURL, setQueryURL) = remember { mutableStateOf("") }
    val (searchResult, setSearchResult) = remember { mutableStateOf<SearchResult?>(null) }
    val (name, setName) = remember { mutableStateOf("") }
    val (addedFolder, setAddedFolder) = remember { mutableStateOf("") }
    val switchFolders = remember {
        folders.map { it.title to false }.toMutableStateMap()
    }
    val url = searchResult?.url.orEmpty

    val search = {
        scope.launch(Dispatchers.IO) {
            val result = searchFeeds(queryURL)
            if (result != null) {
                if (result.name.isNotBlank()) {
                    setName(result.name)
                }
                setSearchResult(result)
            }
        }
    }

    val submitFeed = {
        val existingFolderNames = switchFolders.filter { it.value }.keys
        val folderNames = collectFolders(existingFolderNames, addedFolder)

        if (searchResult != null) {
            onSubmit(
                AddFeedForm(
                    url = searchResult.url,
                    siteURL = searchResult.siteURL,
                    name = name,
                    folderTitles = folderNames
                )
            )
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            TextField(
                value = queryURL,
                onValueChange = setQueryURL,
                readOnly = searchResult != null,
                label = {
                    Text(stringResource(id = R.string.add_feed_url_title))
                },
            )
            if (url.isBlank()) {
                Button(onClick = { search() }) {
                    Text("Search")
                }
            } else {
                TextField(
                    value = name,
                    onValueChange = setName,
                    label = {
                        Text(stringResource(id = R.string.add_feed_name_title))
                    },
                    supportingText = {
                        Text(url)
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
                        Text(stringResource(R.string.feed_form_cancel))
                    }
                    Button(onClick = { submitFeed() }) {
                        Text(stringResource(R.string.add_feed_submit))
                    }
                }
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
fun AddFeedViewPreview() {
    AddFeedView(
        folders = listOf(Folder(title = "Tech")),
        onSubmit = {},
        onCancel = {},
        searchFeeds = { null }
    )
}
