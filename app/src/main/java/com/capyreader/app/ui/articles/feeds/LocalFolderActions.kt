package com.capyreader.app.ui.articles.feeds

import androidx.compose.runtime.compositionLocalOf

val LocalFolderActions = compositionLocalOf { FolderActions() }

data class FolderActions(
    val updateExpanded: (folderName: String, expanded: Boolean) -> Unit = { _, _ -> },
)
