package com.jocmp.basil

import java.net.URL

data class AddFeedForm(
    val url: URL,
    val name: String = "",
    val siteURL: URL? = null,
    val folderTitles: List<String> = emptyList()
)
