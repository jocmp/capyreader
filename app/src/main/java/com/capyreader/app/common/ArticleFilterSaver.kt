package com.capyreader.app.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import com.jocmp.capy.ArticleFilter
import kotlinx.serialization.json.Json

val ArticleFilter.Companion.Saver
    get() = Saver<MutableState<ArticleFilter?>, String>(
        save = { state ->
            state.value?.let { Json.encodeToString(it) }
        },
        restore = { jsonString ->
            mutableStateOf(Json.decodeFromString(jsonString))
        }
    )
