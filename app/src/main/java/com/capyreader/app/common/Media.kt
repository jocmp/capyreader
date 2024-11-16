package com.capyreader.app.common

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val url: String,
    val altText: String?
)
