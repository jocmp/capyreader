package com.jocmp.capy

import kotlinx.serialization.Serializable

@Serializable
data class SavedSearch(
    val id: String,
    val name: String,
    val query: String?,
)
