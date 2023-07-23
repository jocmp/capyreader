package com.jocmp.basilreader.ui.folders

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FoldersList(folders: List<Folder>) {
    LazyColumn {
        items(folders) {
            Text(it.name)
        }
    }
}