package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val id: String,
    val subscriptionID: String,
    val title: String,
    val feedURL: String,
    val siteURL: String = "",
    val folderName: String = "",
    val faviconURL: String? = null,
    override val count: Long = 0
): Countable
