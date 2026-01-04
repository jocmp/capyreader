package com.capyreader.app.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AudioEnclosure(
    val url: String,
    val title: String,
    val feedName: String,
    val durationSeconds: Long?,
    val artworkUrl: String?
) {
    companion object
}

val AudioEnclosure.Companion.Saver
    get() = Saver<MutableState<AudioEnclosure?>, String>(
        save = { state ->
            Json.encodeToString(state.value)
        },
        restore = { jsonString ->
            mutableStateOf(Json.decodeFromString(jsonString))
        }
    )
