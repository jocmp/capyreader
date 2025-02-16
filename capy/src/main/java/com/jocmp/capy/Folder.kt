package com.jocmp.capy

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val title: String,
    val feeds: List<Feed> = emptyList(),
    val expanded: Boolean = false,
    override val count: Long = 0,
) : Countable
