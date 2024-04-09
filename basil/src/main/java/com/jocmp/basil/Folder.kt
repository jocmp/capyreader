package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val title: String,
    val feeds: List<Feed> = emptyList(),
    override val count: Long = 0,
) : Countable
