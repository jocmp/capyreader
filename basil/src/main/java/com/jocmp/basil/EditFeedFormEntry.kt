package com.jocmp.basil

data class EditFeedFormEntry(
    val feedID: String,
    val title: String,
    val folderTitles: List<String> = emptyList()
)
