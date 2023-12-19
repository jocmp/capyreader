package com.jocmp.basil.accounts

internal data class ParsedItem(
    val externalID: String,
    val title: String? = null,
    val contentHTML: String? = null,
    val url: String? = null,
    val summary: String? = null,
    val imageURL: String? = null,
)
