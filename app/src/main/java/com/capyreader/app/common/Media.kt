package com.capyreader.app.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MediaItem(
    val url: String,
    val altText: String?
)

@Serializable
data class Media(
    val images: List<MediaItem>,
    val currentIndex: Int = 0
) {
    constructor(url: String, altText: String?) : this(
        images = listOf(MediaItem(url, altText)),
        currentIndex = 0
    )
}

val Media.Companion.Saver
    get() = Saver<MutableState<Media?>, String>(
        save = { state ->
            Json.encodeToString(state.value)
        },
        restore = { jsonString ->
            mutableStateOf(Json.decodeFromString(jsonString))
        }
    )
