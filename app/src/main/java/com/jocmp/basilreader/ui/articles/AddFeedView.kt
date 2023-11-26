package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basilreader.ui.components.TextField
import kotlin.math.exp

@Composable
fun AddFeedView(
    folders: List<Folder>,
    onSubmit: (feed: Feed) -> Unit,
) {
    val (selectedFolder, selectFolder) = remember {
        mutableStateOf<Folder?>(null)
    }

    val (url, setURL) = remember { mutableStateOf("") }
    val (name, setName) = remember { mutableStateOf("") }

    Column {
        TextField(
            value = url,
            onValueChange = setURL,
        )
        TextField(
            value = name,
            onValueChange = setName,
        )
//        if (folders.isNotEmpty()) {
//            FolderMenuBox(
//                selected = selectedFolder,
//                folders = folders,
//                onFolderSelect = { selectFolder(it) }
//            )
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderMenuBox(
    selected: Folder?,
    folders: List<Folder>,
    onFolderSelect: (folder: Folder) -> Unit,
) {
    val (expanded, setExpanded) = mutableStateOf(false)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(!expanded) }
    ) {
        TextField(
            readOnly = true,
            value = selected?.title ?: "",
            onValueChange = {},
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = true,
            onDismissRequest = {}
        ) {
            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.title) },
                    onClick = {
                        onFolderSelect(folder)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun AddFeedViewPreview() {
    AddFeedView(
        folders = emptyList(),
        onSubmit = {}
    )
}
