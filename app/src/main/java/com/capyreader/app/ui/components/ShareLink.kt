package com.capyreader.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ShareLink(
    val text: String,
    val url: String,
)

val ShareLink.Companion.Saver
    get() = Saver<MutableState<ShareLink?>, String>(
        save = { state ->
            Json.encodeToString(state.value)
        },
        restore = { jsonString ->
            mutableStateOf(Json.decodeFromString(jsonString))
        }
    )

@Composable
fun rememberSaveableShareLink() =
    rememberSaveable(saver = ShareLink.Saver) { mutableStateOf(null) }
