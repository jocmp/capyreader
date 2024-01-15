package com.jocmp.basil

data class EditFeedForm(
    val feedID: String,
    val name: String,
    val folderTitles: List<String> = emptyList()
)
