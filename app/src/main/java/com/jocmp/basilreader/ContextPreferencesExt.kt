package com.jocmp.basilreader

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settings by preferencesDataStore("settings")

fun Preferences.selectedAccount(): String? {
    return get(stringPreferencesKey("account_id"))
}

suspend fun DataStore<Preferences>.selectAccount(id: String) {
    val key = stringPreferencesKey("account_id")

    edit { preferences ->
        preferences[key] = id
    }
}
