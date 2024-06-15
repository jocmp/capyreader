package com.jocmp.capy.opml

internal data class Folder(
    val title: String? = null,
    val text: String? = null,
    val feeds: MutableList<Feed> = mutableListOf(),
    val folders: MutableList<Folder> = mutableListOf()
)
