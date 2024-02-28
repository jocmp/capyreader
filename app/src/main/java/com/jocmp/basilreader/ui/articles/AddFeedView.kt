package com.jocmp.basilreader.ui.articles

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.AddFeedForm
import com.jocmp.basil.Folder
import com.jocmp.basil.accounts.AddFeedResult
import com.jocmp.basil.common.orEmpty
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.TextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddFeedView(
    onAddFeed: (url: String) -> Unit,
) {
    val (queryURL, setQueryURL) = remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            TextField(
                value = queryURL,
                onValueChange = setQueryURL,
                label = {
                    Text(stringResource(id = R.string.add_feed_url_title))
                }
            )
            Button(onClick = { }) {
                Text(stringResource(R.string.add_feed_submit))
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
        onAddFeed = {}
    )
}
