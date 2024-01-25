package com.jocmp.basil

data class AddFeedForm(
    val url: String,
    val name: String = "",
    val folderTitles: List<String> = emptyList()
)
