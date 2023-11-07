package com.jocmp.basil

data class Folder(
    val title: String? = null,
    val text: String? = null,
    val feeds: MutableList<Feed> = mutableListOf()
)
