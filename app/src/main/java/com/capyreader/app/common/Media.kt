package com.capyreader.app.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Media(
    val url: String,
    val altText: String?
)

val Media.Companion.Saver
    get() = Saver<MutableState<Media?>, String>(
        save = { state ->
            Json.encodeToString(state.value)
        },
        restore = { jsonString ->
            mutableStateOf(Json.decodeFromString(jsonString))
        }
    )
