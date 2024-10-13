package com.jocmp.capy

data class EditFeedFormEntry(
    val feedID: String,
    val title: String,
    val enableNotifications: Boolean,
    val folderTitles: List<String> = emptyList()
)
