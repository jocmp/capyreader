package com.jocmp.basilreader

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jocmp.basil.ArticleFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.settings by preferencesDataStore("settings")

val Preferences.selectedAccountID: String?
    get() = get(stringPreferencesKey("account_id"))

val Preferences.filter: ArticleFilter?
    get() = getSerializable("filter")

val Preferences.articleID: String?
    get() = get(stringPreferencesKey("article_id"))

suspend fun DataStore<Preferences>.putAccountID(id: String) {
    edit { preferences ->
        preferences[stringPreferencesKey("account_id")] = id
    }
}

suspend fun DataStore<Preferences>.putArticleID(id: String?) = withContext(Dispatchers.IO) {
    val key = stringPreferencesKey("article_id")

    edit { preferences ->
        if (id.isNullOrBlank()) {
            preferences.remove(key)
        } else {
            preferences[key] = id
        }
    }
}

suspend fun DataStore<Preferences>.putFilter(articleFilter: ArticleFilter) {
    putSerializable("filter", articleFilter)
}

private suspend inline fun <reified S> DataStore<Preferences>.putSerializable(
    key: String,
    value: S
) {
    val jsonString = Json.encodeToString(value)

    edit { preferences ->
        preferences[stringPreferencesKey(key)] = jsonString
    }
}

private inline fun <reified S> Preferences.getSerializable(key: String): S? {
    return get(stringPreferencesKey(key))?.let {
        Json.decodeFromString(it) as? S
    }
}
