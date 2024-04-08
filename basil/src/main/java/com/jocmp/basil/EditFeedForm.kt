package com.jocmp.basil

data class EditFeedForm(
    val feedID: String,
    val title: String,
    val folderTitles: List<String> = emptyList()
)
